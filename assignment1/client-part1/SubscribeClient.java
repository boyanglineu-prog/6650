package model;

import com.google.gson.JsonParser;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ClientEndpoint
public class SubscribeClient {
    private static final int NUM_SUBSCRIBERS = 10; // one per resortID: 1 - 10
    private static final int DEFAULT_RUN_SECONDS = 300; // standalone run length; SwipeClient controls it when used together
    private static final long MAX_BACKOFF_MS = 30_000;
    //    private static final String URL = "ws://localhost:8080/ski-servlet/leaderboard/";
    private static final String URL = "ws://16.148.133.255:8080/ski-servlet/leaderboard/";

    // shared by all connections (callbacks arrive on different threads, so everything is thread-safe)
    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(NUM_SUBSCRIBERS);

    private static final Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<long[]> records = new ConcurrentLinkedQueue<>(); // {receivedAt, serverMillis}

    private static final Map<Integer, AtomicInteger> perResortCount = new ConcurrentHashMap<>(); // updates per subscriber
    private static final Map<Integer, Long> connectTimeMs = new ConcurrentHashMap<>();           // first successful connect only

    private static final AtomicInteger messageCount = new AtomicInteger();
    private static final AtomicInteger parseErrors = new AtomicInteger();
    private static final AtomicInteger connections = new AtomicInteger();     // successful opens (incl. reconnects)
    private static final AtomicInteger reconnects = new AtomicInteger();
    private static final AtomicInteger connectFailures = new AtomicInteger();

    private static volatile boolean shuttingDown = false;
    private static volatile long startMillis;

    private final int resortID;

    public SubscribeClient(int resortID) {
        this.resortID = resortID;
    }

    @OnOpen
    public void onOpen(Session session) {
        // on open, insert to session map
        sessions.put(resortID, session);
        connections.incrementAndGet();
    }

    @OnMessage
    public void onMessage(String msg) {
        // add a record, track the time
        long receivedAt = System.currentTimeMillis();
        messageCount.incrementAndGet();
        perResortCount.computeIfAbsent(resortID, k -> new AtomicInteger()).incrementAndGet();
        try {
            String ts = JsonParser.parseString(msg).getAsJsonObject().get("serverTimestamp").getAsString();
            long serverMillis = Instant.parse(ts).toEpochMilli();
            records.add(new long[]{receivedAt, serverMillis});
        } catch (Exception e) {
            parseErrors.incrementAndGet();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessions.remove(resortID, session); // remove from session map
        if (!shuttingDown) {
            System.err.println("resort " + resortID + " disconnected (" + reason.getReasonPhrase() + "), reconnecting");
            reconnects.incrementAndGet();
            scheduleConnect(resortID, 0, 1000);
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        // onClose normally follows and handles the reconnect
        System.err.println("resort " + resortID + " error: " + t);
    }

    // Run connect() after delayMs. A failed attempt retries with backoff (1s, 2s, 4s ... capped at 30s).
    private static void scheduleConnect(int resortID, int attempt, long delayMs) {
        // if it is a shutdown from our side
        if (shuttingDown || scheduler.isShutdown()) {
            return;
        }
        // else, schedule a reconnect
        scheduler.schedule(() -> connect(resortID, attempt), delayMs, TimeUnit.MILLISECONDS);
    }

    private static void connect(int resortID, int attempt) {
        // if shutdown from our side
        if (shuttingDown) {
            return;
        }

        // else
        long t0 = System.nanoTime();
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(new SubscribeClient(resortID), URI.create(URL + resortID));
            // connection establishment time (handshake), recorded for the first successful connect per resort
            connectTimeMs.putIfAbsent(resortID, (System.nanoTime() - t0) / 1_000_000);
        } catch (Exception e) {
            // if connect fail, wait for some time to reconnect
            connectFailures.incrementAndGet();
            System.err.println("resort " + resortID + " connect failed (attempt " + (attempt + 1) + "): " + e);
            long backoffMs = Math.min(MAX_BACKOFF_MS, 1000L << Math.min(attempt, 5)); // wait for geometry sequence time
            scheduleConnect(resortID, attempt + 1, backoffMs);
        }
    }

    // Open all subscriber connections (returns immediately; the scheduler threads do the connecting).
    public static void start() {
        startMillis = System.currentTimeMillis();
        for (int i = 1; i <= NUM_SUBSCRIBERS; i++) {
            perResortCount.put(i, new AtomicInteger());
            scheduleConnect(i, 0, 0);
            // the thread pool executor will take care of it
        }
    }

    //
    public static boolean awaitConnected(long timeoutMs) throws InterruptedException {

        long deadline = System.currentTimeMillis() + timeoutMs;

        while (sessions.size() < NUM_SUBSCRIBERS) {
            // not fully connect yet
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            Thread.sleep(50);
        }

        return true;
    }

    // time is up: stop reconnecting first, then close the sessions, then print the stats
    public static void stopAndReport() throws InterruptedException {
        long runMillis = System.currentTimeMillis() - startMillis;

        shuttingDown = true;
        scheduler.shutdownNow();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);

        for (Session s : sessions.values()) {
            try {
                s.close();
            } catch (Exception ignored) {
            }
        }

        printStats(runMillis);
    }

    // Standalone mode: java model.SubscribeClient [runSeconds]
    public static void main(String[] args) throws Exception {
        int runSeconds = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_RUN_SECONDS;

        start();

        // keep the connections open for the duration of the run
        // the sessions are running on the library's threads, not on the main thread,
        // thus the main thread just waits
        Thread.sleep(runSeconds * 1000L);

        stopAndReport();
    }

    private static void printStats(long runMillis) {
        System.out.println("=== Subscribe path ===");
        System.out.println("Connections established: " + connections.get()
                + ", reconnects: " + reconnects.get()
                + ", connect failures: " + connectFailures.get());

        List<Long> times = new ArrayList<>(connectTimeMs.values());
        Collections.sort(times);
        if (!times.isEmpty()) {
            long sum = 0;
            for (long t : times) {
                sum += t;
            }
            System.out.println("Connection establishment ms: min=" + times.get(0)
                    + " mean=" + (sum / times.size())
                    + " max=" + times.get(times.size() - 1)
                    + " (n=" + times.size() + ")");
            System.out.println("  per resort: " + new TreeMap<>(connectTimeMs));
        }

        System.out.println("Updates per subscriber (resortID=count): " + new TreeMap<>(perResortCount));
        System.out.println("Total updates received: " + messageCount.get() + ", parse errors: " + parseErrors.get());
        System.out.println("Rate: " + (messageCount.get() * 1000.0 / runMillis) + " msg/s");

        List<Long> latencies = new ArrayList<>();
        int negative = 0;
        for (long[] r : records) {
            long l = r[0] - r[1];
            latencies.add(l);
            if (l < 0) {
                negative++;
            }
        }
        Collections.sort(latencies);

        if (!latencies.isEmpty()) {
            int n = latencies.size();
            long sum = 0;
            for (long l : latencies) {
                sum += l;
            }
            System.out.println("Push latency ms: min=" + latencies.get(0)
                    + " mean=" + (sum / n)
                    + " p50=" + latencies.get(Math.min(n - 1, (int) (n * 0.50)))
                    + " p95=" + latencies.get(Math.min(n - 1, (int) (n * 0.95)))
                    + " p99=" + latencies.get(Math.min(n - 1, (int) (n * 0.99)))
                    + " max=" + latencies.get(n - 1));
            // assumption for the report: receive time (client clock) minus serverTimestamp (server clock),
            // so the value is an approximate one-way push latency that includes clock skew between the two machines
            System.out.println("  note: push latency = client receive time - server timestamp (different clocks, includes skew)");
            if (negative > 0) {
                System.out.println("  note: " + negative + " negative latencies (client/server clock skew)");
            }
        }
    }
}
