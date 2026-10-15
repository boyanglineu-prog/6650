package model;

import java.util.Random;

public class SwipeMessage {
    //    private static final String URL_PREFIX = "http://localhost:8080/ski-servlet/";
    private static final String URL_PREFIX = "http://32.191.63.101:8080/ski-servlet/";
    private static final Random RANDOM = new Random();

    private final String body;
    private final String url;
    private final int resortID;

    public SwipeMessage(String url, String body, int resortID) {
        this.url = url;
        this.body = body;
        this.resortID = resortID;
    }

    public int getResortID() {
        return this.resortID;
    }

    public String getUrl() {
        return this.url;
    }

    public String getBody() {
        return this.body;
    }

    public static SwipeMessage randomMessage() {
        int resortID = RANDOM.nextInt(10) + 1;
        int seasonID = 2026;
        int skierID = RANDOM.nextInt(100000) + 1;
        int dayID = 222;

        int time = RANDOM.nextInt(360) + 1;
        int liftID = RANDOM.nextInt(40) + 1;

        String url = URL_PREFIX + "resorts/" + resortID + "/seasons/" + seasonID + "/days/" + dayID + "/skiers/" + skierID;
        String body = "{\"time\": " + time + ", \"liftID\": " + liftID + "}";

        return new SwipeMessage(url, body, resortID);
    }
}
