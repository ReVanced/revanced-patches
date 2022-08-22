package app.revanced.integrations.patches.downloads;

import app.revanced.integrations.utils.LogHelper;

/**
 * Used by app.revanced.patches.youtube.interaction.downloads.bytecode.patch.DownloadsBytecodePatch
 */
public class DownloadsPatch {
    private static String videoId;

    /**
     * Called when the video changes
     * @param videoId The current video id
     */
    public static void setVideoId(String videoId) {
        LogHelper.debug(DownloadsPatch.class, "newVideoLoaded - " + videoId);

        DownloadsPatch.videoId = videoId;
    }

    /**
     * @return The current video id
     */
    public static String getCurrentVideoId() {
        return videoId;
    }
}
