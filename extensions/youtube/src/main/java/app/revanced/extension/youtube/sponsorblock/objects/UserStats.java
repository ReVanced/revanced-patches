package app.revanced.extension.youtube.sponsorblock.objects;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;

/**
 * SponsorBlock user stats
 */
public class UserStats {
    /**
     * How long to cache user stats objects.
     */
    private static final long STATS_EXPIRATION_MILLISECONDS = 60 * 60 * 1000; // 60 minutes.

    private final String privateUserId;
    public final String publicUserId;
    public final String userName;
    /**
     * "User reputation".  Unclear how SB determines this value.
     */
    public final float reputation;
    /**
     * {@link #segmentCount} plus {@link #ignoredSegmentCount}
     */
    public final int totalSegmentCountIncludingIgnored;
    public final int segmentCount;
    public final int ignoredSegmentCount;
    public final int viewCount;
    public final double minutesSaved;

    /**
     * When this stat was fetched.
     */
    public final long fetchTime;

    public UserStats(String privateSbId, @NonNull JSONObject json) throws JSONException {
        privateUserId = privateSbId;
        publicUserId = json.getString("userID");
        userName = json.getString("userName");
        reputation = (float)json.getDouble("reputation");
        segmentCount = json.getInt("segmentCount");
        ignoredSegmentCount = json.getInt("ignoredSegmentCount");
        totalSegmentCountIncludingIgnored = segmentCount + ignoredSegmentCount;
        viewCount = json.getInt("viewCount");
        minutesSaved = json.getDouble("minutesSaved");
        fetchTime = System.currentTimeMillis();
    }

    public boolean isExpired() {
        if (STATS_EXPIRATION_MILLISECONDS < System.currentTimeMillis() - fetchTime) {
            return true;
        }

        // User changed their SB private user id.
        return !SponsorBlockSettings.userHasSBPrivateId()
                || !SponsorBlockSettings.getSBPrivateUserID().equals(privateUserId);
    }

    @NonNull
    @Override
    public String toString() {
        // Do not include private user id in toString().
        return "UserStats{"
                + "publicUserId='" + publicUserId + '\''
                + ", userName='" + userName + '\''
                + ", reputation=" + reputation
                + ", segmentCount=" + segmentCount
                + ", ignoredSegmentCount=" + ignoredSegmentCount
                + ", viewCount=" + viewCount
                + ", minutesSaved=" + minutesSaved
                + '}';
    }
}