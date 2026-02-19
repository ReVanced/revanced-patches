package app.revanced.extension.tiktok.download;

import com.ss.android.ugc.aweme.feed.model.Video;

@SuppressWarnings("unused")
public class DownloadsPatch {

    public static String getDownloadPath() {
        return "Pictures/Tiktok";
        //return Settings.DOWNLOAD_PATH.get();
    }

    public static boolean shouldRemoveWatermark() {
        return true;
        //return Settings.DOWNLOAD_WATERMARK.get();
    }

    public static void patchVideoObject(Video video) {
        if (video == null) return;

        try {
            boolean isMissingCleanUrl = false;
            
            // non-watermark url is removed by tiktok for some videos (licensing/user restrictions)
            if (video.downloadNoWatermarkAddr == null) {
                isMissingCleanUrl = true;
            } else if (video.downloadNoWatermarkAddr.getUrlList() == null || video.downloadNoWatermarkAddr.getUrlList().isEmpty()) {
                isMissingCleanUrl = true;
            }

            // overwrite field with the play address if empty
            if (isMissingCleanUrl) {
                if (video.h264PlayAddr != null && video.h264PlayAddr.getUrlList() != null && !video.h264PlayAddr.getUrlList().isEmpty()) {
                    video.downloadNoWatermarkAddr = video.h264PlayAddr;
                } else if (video.playAddr != null) {
                    // fallback
                    video.downloadNoWatermarkAddr = video.playAddr;
                }
            }
        } catch (Throwable t) {
        }
    }
}