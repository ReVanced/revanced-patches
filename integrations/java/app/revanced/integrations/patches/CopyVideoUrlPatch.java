package app.revanced.integrations.patches;

import static app.revanced.integrations.utils.StringRef.str;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class CopyVideoUrlPatch {
    public static void copyUrl(Boolean withTimestamp) {
        try {
            String url = String.format("https://youtu.be/%s", VideoInformation.getCurrentVideoId());
            if (withTimestamp) {
                long seconds = VideoInformation.getVideoTime() / 1000;
                url += String.format("?t=%s", seconds);
            }

            ReVancedUtils.setClipboard(url);
            ReVancedUtils.showToastShort(str("share_copy_url_success"));
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to generate video url", e);
        }
    }
}
