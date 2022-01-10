package fi.vanced.libraries.youtube.player;

import static fi.razerman.youtube.XGlobals.debug;

import android.util.Log;

import fi.vanced.libraries.youtube.ryd.ReturnYouTubeDislikes;

public class VideoInformation {
    private static final String TAG = "VI - VideoInfo";

    public static String currentVideoId;
    public static Integer dislikeCount = null;
    public static String channelName = null;
    public static long lastKnownVideoTime = -1L;

    // Call hook in the YT code when the video changes
    public static void setCurrentVideoId(final String videoId) {
        if (videoId == null) {
            if (debug) {
                Log.d(TAG, "setCurrentVideoId - new id was null - currentVideoId was" + currentVideoId);
            }
            currentVideoId = null;
            dislikeCount = null;
            channelName = null;
            return;
        }

        if (videoId.equals(currentVideoId)) {
            if (debug) {
                Log.d(TAG, "setCurrentVideoId - new and current video were equal - " + videoId);
            }
            return;
        }

        if (debug) {
            Log.d(TAG, "setCurrentVideoId - video id updated from " + currentVideoId + " to " + videoId);
        }

        currentVideoId = videoId;

        // New video
        ReturnYouTubeDislikes.newVideoLoaded(videoId);
    }
}
