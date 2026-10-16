# CS6650 Assignment 1: Results and Analysis

Main run: 500 sender threads, 500,000 swipes, client on a laptop and server on AWS EC2 (Tomcat 9). The design, threading model, and Little's Law prediction are in [`../design_document.md`](../design_document.md). Raw data: [`swipe_metrics.csv`](swipe_metrics.csv); console output: `part_1_output.png`, `part_2_output.png`.

## Results

| Metric | Value |
|---|---|
| Successful / failed swipes | 500,000 / 0 (138 retries after connection errors, all recovered) |
| Main run wall-clock | 54.6 s (warmup: 31.8 s for 32,000 swipes) |
| Throughput | 9,162 swipes/s (about 915/s per resort, evenly spread) |
| Latency, ms | mean 54.1, median 45.2, p95 106.6, p99 223.1, min 14.6, max 2,362 |
| Subscribers | 10 connections, 0 reconnects, handshake 210-215 ms, 178 updates each (1,780 total, about 19.9/s) |

## Throughput over time

![Ingest throughput over time, 10-second buckets](throughput_10s.png)

About 7,900 swipes/s in the first 10 s (connection ramp-up), then flat at about 9,400-9,500 swipes/s.

## Analysis

- **Why the prediction was missed:** Little's Law only predicts the maximum when W stays constant. Under load W grew from 21 ms to 54 ms, which is queueing. Using the loaded W gives 500 / 0.0541 = 9,242 swipes/s, which matches the measured rate. Scaling was also sublinear: 32 warmup threads gave about 1,000 swipes/s, and 500 threads gave 9,162.
- **The queue is not the bottleneck:** average in-flight requests = 9,162 x 0.0541 s = 496, which matches the 500 threads. The sender threads spend essentially all their time waiting on HTTP responses, not on the queue.
- **As observed, EC2 instance CPU utility goes to 180% at peak, thus became the bottleneck of throughput.**
