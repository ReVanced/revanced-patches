package app.revanced.integrations.returnyoutubedislike.requests;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * ReturnYouTubeDislike API estimated like/dislike/view counts.
 *
 * ReturnYouTubeDislike does not guarantee when the counts are updated.
 * So these values may lag behind what YouTube shows.
 */
public final class RYDVoteData {

    public final String videoId;

    /**
     * Estimated number of views
     */
    public final long viewCount;

    /**
     * Estimated like count
     */
    public final long likeCount;

    /**
     * Estimated dislike count
     */
    public final long dislikeCount;

    /**
     * Estimated percentage of likes for all votes.  Value has range of [0, 1]
     *
     * A video with 400 positive votes, and 100 negative votes, has a likePercentage of 0.8
     */
    public final float likePercentage;

    /**
     * Estimated percentage of dislikes for all votes. Value has range of [0, 1]
     *
     * A video with 400 positive votes, and 100 negative votes, has a dislikePercentage of 0.2
     */
    public final float dislikePercentage;

    /**
     * @throws JSONException if JSON parse error occurs, or if the values make no sense (ie: negative values)
     */
    public RYDVoteData(JSONObject json) throws JSONException {
        Objects.requireNonNull(json);
        videoId = json.getString("id");
        viewCount = json.getLong("viewCount");
        likeCount = json.getLong("likes");
        dislikeCount = json.getLong("dislikes");
        if (likeCount < 0 || dislikeCount < 0 || viewCount < 0) {
            throw new JSONException("Unexpected JSON values: " + json);
        }
        likePercentage = (likeCount == 0 ? 0 : (float)likeCount / (likeCount + dislikeCount));
        dislikePercentage = (dislikeCount == 0 ? 0 : (float)dislikeCount / (likeCount + dislikeCount));
    }

    @Override
    public String toString() {
        return "RYDVoteData{"
                + "videoId=" + videoId
                + ", viewCount=" + viewCount
                + ", likeCount=" + likeCount
                + ", dislikeCount=" + dislikeCount
                + ", likePercentage=" + likePercentage
                + ", dislikePercentage=" + dislikePercentage
                + '}';
    }

    // equals and hashcode is not implemented (currently not needed)
}
