package app.revanced.integrations.sponsorblock.objects;

public class UserStats {
    private final String userName;
    private final double minutesSaved;
    private final int segmentCount;
    private final int viewCount;

    public UserStats(String userName, double minutesSaved, int segmentCount, int viewCount) {
        this.userName = userName;
        this.minutesSaved = minutesSaved;
        this.segmentCount = segmentCount;
        this.viewCount = viewCount;
    }

    public String getUserName() {
        return userName;
    }

    public double getMinutesSaved() {
        return minutesSaved;
    }

    public int getSegmentCount() {
        return segmentCount;
    }

    public int getViewCount() {
        return viewCount;
    }
}