package app.revanced.extension.youtube.returnyoutubedislike.requests;

import static app.revanced.extension.youtube.returnyoutubedislike.ReturnYouTubeDislike.Vote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import app.revanced.extension.shared.Logger;

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
    private volatile long likeCount; // Read/write from different threads.
    /**
     * Like count can be hidden by video creator, but RYD still tracks the number
     * of like/dislikes it received thru it's browser extension and and API.
     * The raw like/dislikes can be used to calculate a percentage.
     *
     * Raw values can be null, especially for older videos with little to no views.
     */
    @Nullable
    private final Long fetchedRawLikeCount;
    private volatile float likePercentage;

    private final long fetchedDislikeCount;
    private volatile long dislikeCount; // Read/write from different threads.
    @Nullable
    private final Long fetchedRawDislikeCount;
    private volatile float dislikePercentage;

    @Nullable
    private static Long getLongIfExist(JSONObject json, String key) throws JSONException {
        return json.isNull(key)
                ? null
                : json.getLong(key);
    }

    /**
     * @throws JSONException if JSON parse error occurs, or if the values make no sense (ie: negative values)
     */
    public RYDVoteData(@NonNull JSONObject json) throws JSONException {
        videoId = json.getString("id");
        viewCount = json.getLong("viewCount");

        fetchedLikeCount = json.getLong("likes");
        fetchedRawLikeCount = getLongIfExist(json, "rawLikes");

        fetchedDislikeCount = json.getLong("dislikes");
        fetchedRawDislikeCount = getLongIfExist(json, "rawDislikes");

        if (viewCount < 0 || fetchedLikeCount < 0 || fetchedDislikeCount < 0) {
            throw new JSONException("Unexpected JSON values: " + json);
        }
        likeCount = fetchedLikeCount;
        dislikeCount = fetchedDislikeCount;

        updateUsingVote(Vote.LIKE_REMOVE); // Calculate percentages.
    }

    /**
     * Public like count of the video, as reported by YT when RYD last updated it's data.
     *
     * If the likes were hidden by the video creator, then this returns an
     * estimated likes using the same extrapolation as the dislikes.
     */
    public long getLikeCount() {
        return likeCount;
    }

    /**
     * Estimated total dislike count, extrapolated from the public like count using RYD data.
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
        final int likesToAdd, dislikesToAdd;

        switch (vote) {
            case LIKE:
                likesToAdd = 1;
                dislikesToAdd = 0;
                break;
            case DISLIKE:
                likesToAdd = 0;
                dislikesToAdd = 1;
                break;
            case LIKE_REMOVE:
                likesToAdd = 0;
                dislikesToAdd = 0;
                break;
            default:
                throw new IllegalStateException();
        }

        // If a video has no public likes but RYD has raw like data,
        // then use the raw data instead.
        final boolean videoHasNoPublicLikes = fetchedLikeCount == 0;
        final boolean hasRawData = fetchedRawLikeCount != null && fetchedRawDislikeCount != null;

        if (videoHasNoPublicLikes && hasRawData && fetchedRawDislikeCount > 0) {
            // YT creator has hidden the likes count, and this is an older video that
            // RYD does not provide estimated like counts.
            //
            // But we can calculate the public likes the same way RYD does for newer videos with hidden likes,
            // by using the same raw to estimated scale factor applied to dislikes.
            // This calculation exactly matches the public likes RYD provides for newer hidden videos.
            final float estimatedRawScaleFactor = (float) fetchedDislikeCount / fetchedRawDislikeCount;
            likeCount = (long) (estimatedRawScaleFactor * fetchedRawLikeCount) + likesToAdd;
            Logger.printDebug(() -> "Using locally calculated estimated likes since RYD did not return an estimate");
        } else {
            likeCount = fetchedLikeCount + likesToAdd;
        }
        // RYD now always returns an estimated dislike count, even if the likes are hidden.
        dislikeCount = fetchedDislikeCount + dislikesToAdd;

        // Update percentages.

        final float totalCount = likeCount + dislikeCount;
        if (totalCount == 0) {
            likePercentage = 0;
            dislikePercentage = 0;
        } else {
            likePercentage = likeCount / totalCount;
            dislikePercentage = dislikeCount / totalCount;
        }
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
