package app.revanced.integrations.youtube.returnyoutubedislike.requests;

import static app.revanced.integrations.youtube.returnyoutubedislike.ReturnYouTubeDislike.Vote;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ReturnYouTubeDislike API estimated like/dislike/view counts.
 *
 * ReturnYouTubeDislike does not guarantee when the counts are updated.
 * So these values may lag behind what YouTube shows.
 */
public final class RYDVoteData {
    @NonNull
    public final String videoId;

    /**
     * Estimated number of views
     */
    public final long viewCount;

    private final long fetchedLikeCount;
    private volatile long likeCount; // read/write from different threads
    private volatile float likePercentage;

    private final long fetchedDislikeCount;
    private volatile long dislikeCount; // read/write from different threads
    private volatile float dislikePercentage;

    /**
     * @throws JSONException if JSON parse error occurs, or if the values make no sense (ie: negative values)
     */
    public RYDVoteData(@NonNull JSONObject json) throws JSONException {
        videoId = json.getString("id");
        viewCount = json.getLong("viewCount");
        fetchedLikeCount = json.getLong("likes");
        fetchedDislikeCount = json.getLong("dislikes");
        if (viewCount < 0 || fetchedLikeCount < 0 || fetchedDislikeCount < 0) {
            throw new JSONException("Unexpected JSON values: " + json);
        }
        likeCount = fetchedLikeCount;
        dislikeCount = fetchedDislikeCount;
        updatePercentages();
    }

    /**
     * Estimated like count
     */
    public long getLikeCount() {
        return likeCount;
    }

    /**
     * Estimated dislike count
     */
    public long getDislikeCount() {
        return dislikeCount;
    }

    /**
     * Estimated percentage of likes for all votes.  Value has range of [0, 1]
     *
     * A video with 400 positive votes, and 100 negative votes, has a likePercentage of 0.8
     */
    public float getLikePercentage() {
        return likePercentage;
    }

    /**
     * Estimated percentage of dislikes for all votes. Value has range of [0, 1]
     *
     * A video with 400 positive votes, and 100 negative votes, has a dislikePercentage of 0.2
     */
    public float getDislikePercentage() {
        return dislikePercentage;
    }

    public void updateUsingVote(Vote vote) {
        switch (vote) {
            case LIKE:
                likeCount = fetchedLikeCount + 1;
                dislikeCount = fetchedDislikeCount;
                break;
            case DISLIKE:
                likeCount = fetchedLikeCount;
                dislikeCount = fetchedDislikeCount + 1;
                break;
            case LIKE_REMOVE:
                likeCount = fetchedLikeCount;
                dislikeCount = fetchedDislikeCount;
                break;
            default:
                throw new IllegalStateException();
        }
        updatePercentages();
    }

    private void updatePercentages() {
        likePercentage = (likeCount == 0 ? 0 : (float) likeCount / (likeCount + dislikeCount));
        dislikePercentage = (dislikeCount == 0 ? 0 : (float) dislikeCount / (likeCount + dislikeCount));
    }

    @NonNull
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
