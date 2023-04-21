package app.revanced.integrations.sponsorblock.objects;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SponsorBlock user stats
 */
public class UserStats {
    @NonNull
    public final String publicUserId;
    @NonNull
    public final String userName;
    /**
     * "User reputation".  Unclear how SB determines this value.
     */
    public final float reputation;
    public final int segmentCount;
    public final int viewCount;
    public final double minutesSaved;

    public UserStats(@NonNull JSONObject json) throws JSONException {
        publicUserId = json.getString("userID");
        userName = json.getString("userName");
        reputation = (float)json.getDouble("reputation");
        segmentCount = json.getInt("segmentCount");
        viewCount = json.getInt("viewCount");
        minutesSaved = json.getDouble("minutesSaved");
    }

    @NonNull
    @Override
    public String toString() {
        return "UserStats{"
                + "publicUserId='" + publicUserId + '\''
                + ", userName='" + userName + '\''
                + ", reputation=" + reputation
                + ", segmentCount=" + segmentCount
                + ", viewCount=" + viewCount
                + ", minutesSaved=" + minutesSaved
                + '}';
    }
}