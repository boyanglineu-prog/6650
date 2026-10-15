package model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SwipeClient {
    private static final int SC_CREATED = 201;

    private static final int NUM_THREADS = 100;
    private static final int NUN_WARMUP_MESSAGES = 1_000;
    private static final int NUM_SWIPE_MESSAGE = 500_000;

    private static final int MAX_RETRIES = 5;
    private static final int RTT_SAMPLES = 100;

    private static final int SIZE_OF_QUEUE = 50_000;
    private static final String CSV_FILE = "swipe_metrics.csv";

    // THE client
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // one per swipe
    private static class SwipeRecord {
        final int resortID;
        long startTimeMs;
        double latencyMs;
        int status;

        SwipeRecord(int resortID) {
            this.resortID = resortID;
        }
    }

    // result per run, carries all the records
    private static class PhaseResult {
        int success;
        int failure;
        int retries;
        int connectionErrors;
        long elapsedMs;
        boolean timedOut;
        List<SwipeRecord> records = new ArrayList<>();

        double throughput() {
            return success * 1000.0 / elapsedMs;
        }
    }

    private static HttpRequest buildRequest(SwipeMessage message) {

        return HttpRequest.newBuilder()
                .uri(URI.create(message.getUrl()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(message.getBody()))
                .build();
    }

    // each send will generate a record, no matter success or failure
    private static SwipeRecord sendWithRetry(SwipeMessage message, AtomicInteger retries, AtomicInteger connectionErrors) {

        HttpRequest request = buildRequest(message);
        SwipeRecord record = new SwipeRecord(message.getResortID()); // for now the content is empty

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            record.startTimeMs = System.currentTimeMillis();
            long t0 = System.nanoTime();

            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                record.latencyMs = (System.nanoTime() - t0) / 1_000_000.0; // 1 microsecond = 1,000,000 nanoseconds
                record.status = response.statusCode();
                if (record.status < 500) {
                    return record;   // e.g. 201 success
                }
            } catch (InterruptedException e) {
                record.latencyMs = (System.nanoTime() - t0) / 1_000_000.0;
                record.status = 0;
                Thread.currentThread().interrupt();
                return record;
            } catch (Exception e) {
                record.latencyMs = (System.nanoTime() - t0) / 1_000_000.0;
                record.status = 0;
                connectionErrors.incrementAndGet();
            }

            if (attempt < MAX_RETRIES) {
                retries.incrementAndGet();
                try {
                    Thread.sleep(50L << attempt); // exponential sleep time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return record;
                }
            }
        }
        return record;
    }

    private static PhaseResult runPhase(int totalMessages) throws InterruptedException {

        BlockingQueue<SwipeMessage> queue = new LinkedBlockingQueue<>(SIZE_OF_QUEUE);
        ConcurrentLinkedQueue<SwipeRecord> records = new ConcurrentLinkedQueue<>();
        // will be part of PhaseResult

        // stats
        AtomicInteger claimed = new AtomicInteger();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failure = new AtomicInteger();
        AtomicInteger retries = new AtomicInteger();
        AtomicInteger connectionErrors = new AtomicInteger();

        // dedicated thread to generate messages into a blocking queue
        Thread generator = new Thread(() -> {
            try {
                for (int i = 0; i < totalMessages; i++) {
                    queue.put(SwipeMessage.randomMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "message-generator");
        generator.start();
        // don't forget to .join() later

        // thread pool for ingesting
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            pool.submit(() -> {
                while (!Thread.currentThread().isInterrupted() && claimed.getAndIncrement() < totalMessages) {
                    SwipeMessage message;

                    try {
                        message = queue.take();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    SwipeRecord record = sendWithRetry(message, retries, connectionErrors);
                    records.add(record);

                    if (record.status == SC_CREATED) {
                        success.incrementAndGet();
                    } else {
                        failure.incrementAndGet();
                    }
                }
            });
        }
        pool.shutdown();

        boolean finished = pool.awaitTermination(10, TimeUnit.MINUTES);
        long elapsed = System.currentTimeMillis() - start;
        if (!finished) {
            System.out.println("WARNING: timed out, results are partial");
            pool.shutdownNow();
            generator.interrupt();
        }

        generator.join();

        // generate phase result
        PhaseResult result = new PhaseResult();
        result.success = success.get();
        result.failure = failure.get();
        result.retries = retries.get();
        result.connectionErrors = connectionErrors.get();
        result.elapsedMs = elapsed;
        result.timedOut = !finished;
        result.records = new ArrayList<>(records);

        return result;
    }

    // measure round-trip time using a set of random test swipes
    private static double[] measureRtt() {

        double coldMs = timedSendMs();

        double sum = 0;
        for (int i = 0; i < RTT_SAMPLES; i++) {
            sum += timedSendMs();
        }

        double warmMS = sum / RTT_SAMPLES;

        return new double[]{coldMs, warmMS};
    }

    // single shot
    private static double timedSendMs() {
        return sendWithRetry(SwipeMessage.randomMessage(),
                new AtomicInteger(),
                new AtomicInteger()).latencyMs;
    }

    private static Path writeCsv(List<SwipeRecord> records) throws IOException {
        Path path = Paths.get(CSV_FILE);
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            w.write("timestamp,requestType,latency,statusCode,resortID\n");
            for (SwipeRecord r : records) {
                w.write(r.startTimeMs + ",SWIPE," + String.format(Locale.ROOT, "%.3f", r.latencyMs)
                        + "," + r.status + "," + r.resortID + "\n");
            }
        }
        return path.toAbsolutePath();
    }

    private static void printIngestLatency(List<SwipeRecord> records) {
        List<Double> latencies = new ArrayList<>();
        for (SwipeRecord r : records) {
            if (r.status == SC_CREATED) {
                latencies.add(r.latencyMs);
            }
        }
        Collections.sort(latencies);
        if (latencies.isEmpty()) {
            return;
        }
        int n = latencies.size();
        double sum = 0;
        for (double l : latencies) {
            sum += l;
        }
        System.out.printf(Locale.ROOT,
                "Ingest latency ms (successful swipes): mean=%.2f median=%.2f p95=%.2f p99=%.2f min=%.2f max=%.2f%n",
                sum / n,
                latencies.get(Math.min(n - 1, (int) (n * 0.50))),
                latencies.get(Math.min(n - 1, (int) (n * 0.95))),
                latencies.get(Math.min(n - 1, (int) (n * 0.99))),
                latencies.get(0),
                latencies.get(n - 1));
    }

    private static void printPerResortThroughput(List<SwipeRecord> records, long elapsedMs) {
        Map<Integer, Integer> perResort = new TreeMap<>();
        for (SwipeRecord r : records) {
            if (r.status == SC_CREATED) {
                perResort.merge(r.resortID, 1, Integer::sum);
            }
        }
        System.out.println("Throughput per resort:");
        for (Map.Entry<Integer, Integer> e : perResort.entrySet()) {
            System.out.printf(Locale.ROOT, "  resort %d: %d swipes, %.1f swipes/s%n",
                    e.getKey(), e.getValue(), e.getValue() * 1000.0 / elapsedMs);
        }
    }

    public static void main(String[] args) throws Exception {

        // SubscribeClient need to be running alongside
        SubscribeClient.start();
        if (!SubscribeClient.awaitConnected(10_000)) {
            // Wait for 10 seconds to see if all ws connect successfully
            System.out.println("WARNING: not all subscribers connected before the ingest started");
        }

        // Little's Law: throughput = requests / response time

        double[] rtt = measureRtt(); // one time test
        double coldMs = rtt[0];
        double warmMs = rtt[1];
        double predicted = NUM_THREADS / (warmMs / 1000.0);

        PhaseResult warmupResult = runPhase(NUN_WARMUP_MESSAGES);   // its records are not used for the stats or CSV
        PhaseResult runResult = runPhase(NUM_SWIPE_MESSAGE);

        System.out.println("=== Ingest path ===");
        // warm up
        System.out.println("Warmup:");
        System.out.println(warmupResult.success + " ok / " + warmupResult.failure + " failed in " + warmupResult.elapsedMs + " ms\n");

        // main run
        System.out.println("Main run:");
        System.out.println("Successful swipes: " + runResult.success);
        System.out.println("Failed swipes: " + runResult.failure);
        System.out.println("Retries: " + runResult.retries);
        System.out.println("connection errors (IO/timeout): " + runResult.connectionErrors + "\n");

        System.out.println("Worker threads: " + NUM_THREADS);
        System.out.println("Wall-clock runtime (main run): " + runResult.elapsedMs + " ms");
        System.out.println("Wall-clock runtime (warmup + main): " + (warmupResult.elapsedMs + runResult.elapsedMs) + " ms\n");

        System.out.println("Throughput: " + String.format(Locale.ROOT, "%.1f", runResult.throughput()) + " swipes/s");

        System.out.println();
        printPerResortThroughput(runResult.records, runResult.elapsedMs);

        System.out.println();
        printIngestLatency(runResult.records);

        System.out.println("Per-swipe CSV written to: " + writeCsv(runResult.records) + "\n");

        System.out.println("=== Little's Law ===");
        System.out.println("Single-swipe RTT: first request (incl. connection setup) " + String.format(Locale.ROOT, "%.1f", coldMs)
                + " ms, warm average " + String.format(Locale.ROOT, "%.1f", warmMs) + " ms");
        System.out.println("Predicted max throughput = L / W = " + NUM_THREADS + " / "
                + String.format(Locale.ROOT, "%.4f", warmMs / 1000.0) + " s = " + String.format(Locale.ROOT, "%.1f", predicted) + " swipes/s");
        System.out.println("Actual: " + String.format(Locale.ROOT, "%.1f", runResult.throughput()) + " swipes/s ("
                + String.format(Locale.ROOT, "%.0f", runResult.throughput() / predicted * 100) + "% of predicted)\n");

        SubscribeClient.stopAndReport();
    }
}
