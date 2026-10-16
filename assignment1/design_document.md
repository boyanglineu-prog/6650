## CS6650 Assignment 1: Design Document

**Name: Boyang Li**
**Project repository:** https://github.com/boyanglineu-prog/6650 

### 1. Architecture

```mermaid
flowchart LR
    subgraph Client["Client (laptop)"]
        direction TB
        Gen["Generator thread"] --> Q[("BlockingQueue<br/>10,000 slots")]
        Q --> Senders["Sender pool<br/>500 threads, shared HttpClient"]
        Subs["SubscribeClient x10<br/>1 WebSocket per resort<br/>reconnect with backoff"]
    end
    subgraph Server["Server (AWS EC2, Tomcat 9, /ski-servlet)"]
        direction TB
        Servlet["SkierServlet<br/>POST /resorts/.../skiers/id<br/>GET /health, /stats"]
        LB["LeaderboardEndpoint<br/>/leaderboard/resortID"]
        Pusher["Pusher thread<br/>every 500 ms"]
        Servlet --> Stats
        Pusher --> LB
    end
    Senders -- "REST: POST swipe" --> Servlet
    Servlet -. "201 Created / 400" .-> Senders
    LB == "WebSocket push" ==> Subs
```


### 2. Major classes

**Server**

- `SkierServlet` handles `/resorts/*`, `/health`, and `/stats` (mapped in `web.xml`).
- `LeaderboardEndpoint` is the WebSocket endpoint. It keeps a map of sessions, and one scheduled thread pushes the leaderboard to every subscriber every 500 ms.
- `RequestStats` holds thread-safe GET and POST counters (`AtomicLong`).

**Client**

- `SwipeClient` is the orchestrator. It starts the subscribers, measures the round-trip time, runs the warmup and then the main phase, prints the metrics, and writes `swipe_metrics.csv`. It uses `SwipeMessage` to create the requests and `SubscribeClient` for the WebSocket side.
- `SubscribeClient` is the WebSocket client. It keeps one connection per resort.

### 3. Threading model (client)

**Run order (main thread):**

![Threading model: run order of the main thread](diagrams/threading_run_order.png)

**Inside each phase (warmup and main run):**

![Threading model: generator, queue, sender threads, HttpClient](diagrams/threading_phase.png)

- **Generator (1 dedicated thread):** creates `SwipeMessage`s and `put`s them on a bounded `LinkedBlockingQueue` (10,000 slots). 
- **Sender pool (fixed thread pool):** each worker `take`s a message and sends it with a **single shared `HttpClient`** 
- **Failure handling:** up to 5 retries with backoff (50 ms x 2^attempt) on connection errors or 5xx; 4xx are not retried; 10 s request timeout.
- **Phases:** (1) *RTT probe*: one cold request plus 100 sequential requests; (2) *warmup*: 32 threads x 1,000 = 32,000 swipes; (3) *main run*: 500 threads, 500,000 swipes.
- **Metrics:** counters are atomic; each swipe records `{start time, latency, status, resortID}` in a `ConcurrentLinkedQueue`, written to CSV after the run.

### 4. WebSocket connection management (client)

- **Subscription pool:** 10 subscribers, one per resort (1-10), connected through a 10-thread `ScheduledExecutorService`.
- **Reconnect:** `@OnClose` schedules a reconnect after 1 s. A failed connect retries with exponential backoff (1, 2, 4, ... capped at 30 s).
- **Metrics:** connections and reconnects, handshake time per resort, updates per subscriber, push latency (receive time minus `serverTimestamp`).

### 5. Little's Law

Throughput = L / W, where L = concurrent requests in flight and W = time per request.

- **L = 500** (one in-flight request per sender thread).
- **W = 21.0 ms**: average of 100 sequential requests.
- **Predicted maximum**: 500 / 0.0210 s = 23,864 swipes/s.
- **Measured**: 9,162 swipes/s (38% of predicted).
