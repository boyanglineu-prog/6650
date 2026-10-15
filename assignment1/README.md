# CS6650 Assignment 1

A servlet-based REST API with WebSocket endpoints, a multithreaded load-test client, and WebSocket subscribers.

## Endpoints

Context path: `/ski-servlet` (port 8080)

| Endpoint | Description |
|---|---|
| `POST /resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}` | Record a lift ride. Body: `{"time": 1-360, "liftID": 1-40}`. Returns `201`, or `400` for invalid input. |
| `GET /resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}` | Fetch a lift ride |
| `GET /health` | Server status |
| `GET /stats` | Request counters |
| `ws://<host>:8080/ski-servlet/echo` | WebSocket echo |
| `ws://<host>:8080/ski-servlet/leaderboard/{resortID}` | WebSocket leaderboard push (a message about every 500 ms) |

## Requirements

- JDK 11 or newer (the code targets Java 11)
- Apache Tomcat 9.x (tested with 9.0.65; Tomcat 10+ will not work, the code uses `javax.*`)
- Port 8080 reachable (on EC2, open it in the security group)

## 1. Run the server

1. Copy the WAR into Tomcat's `webapps/` folder, **named `ski-servlet.war`**. The file name sets the context path.
   ```bash
   cp server/ski-servlet-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/ski-servlet.war
   ```
   On EC2:
   ```bash
   scp -i <key>.pem server/ski-servlet-1.0-SNAPSHOT.war ec2-user@<EC2_IP>:/tmp/
   ssh -i <key>.pem ec2-user@<EC2_IP>
   sudo mv /tmp/ski-servlet-1.0-SNAPSHOT.war /opt/tomcat9/webapps/ski-servlet.war
   ```
2. Start Tomcat:
   ```bash
   $TOMCAT_HOME/bin/startup.sh       # EC2: sudo /opt/tomcat9/bin/startup.sh
   ```
3. Check that it is up:
   ```bash
   curl http://<host>:8080/ski-servlet/health
   ```
   You should get HTTP 200. The home page `http://<host>:8080/ski-servlet/` should also load.

To redeploy a new WAR, first remove the old deployment (`webapps/ski-servlet*` and `work/Catalina/localhost/ski-servlet*`), then copy the new WAR in.

## 2. Run the client

`SwipeClient` is the only class you run. It starts the 10 WebSocket subscribers itself, runs the warmup and the main load test, and prints all metrics.

**Step 1: change the server URL to your EC2 instance's public IP address.**
The address is hardcoded in two files in `client-part2/`. Replace the IP in both (use `localhost` if Tomcat runs on your own machine):

| File | Line to change |
|---|---|
| `SwipeMessage.java` | `URL_PREFIX = "http://<EC2_IP>:8080/ski-servlet/";` |
| `SubscribeClient.java` | `URL = "ws://<EC2_IP>:8080/ski-servlet/leaderboard/";` |

**Step 2: download, compile, and run** (copy and paste, from this folder):

```bash
cd client-part2
mkdir -p libs out
curl -L -o libs/gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
curl -L -o libs/tyrus-standalone-client-1.21.jar https://repo1.maven.org/maven2/org/glassfish/tyrus/bundles/tyrus-standalone-client/1.21/tyrus-standalone-client-1.21.jar
javac -cp "libs/*" -d out SwipeMessage.java SubscribeClient.java SwipeClient.java
java -cp "out:libs/*" model.SwipeClient
```

On Windows, use `;` instead of `:` in the classpath (`"out;libs/*"`). The server must already be running.

**Output:** the console shows ingest metrics (warmup, successes and failures, throughput, latency), the Little's Law comparison, and the subscriber metrics (connections, updates per subscriber, push latency). A per-swipe CSV, `swipe_metrics.csv`, is written to the folder you ran it from.
