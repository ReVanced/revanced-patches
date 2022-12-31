package app.revanced.integrations.patches;

import android.content.Context;
import android.widget.Toast;

import app.revanced.integrations.sponsorblock.StringRef;
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

            Context context = ReVancedUtils.getContext();

            ReVancedUtils.setClipboard(url);
            if (context != null) Toast.makeText(context, StringRef.str("share_copy_url_success"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to generate video url", e);
        }
    }
}
