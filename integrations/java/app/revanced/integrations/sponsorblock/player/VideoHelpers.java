package app.revanced.integrations.sponsorblock.player;

import android.content.Context;
import android.widget.Toast;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class VideoHelpers {

    public static void copyVideoUrlToClipboard() {
        generateVideoUrl(false);
    }

    public static void copyVideoUrlWithTimeStampToClipboard() {
        generateVideoUrl(true);
    }

    private static void generateVideoUrl(boolean appendTimeStamp) {
        try {
            String videoId = VideoInformation.currentVideoId;
            if (videoId == null || videoId.isEmpty()) {
                LogHelper.debug(VideoHelpers.class, "VideoId was empty");
                return;
            }

            String videoUrl = String.format("https://youtu.be/%s", videoId);
            if (appendTimeStamp) {
                long videoTime = VideoInformation.lastKnownVideoTime;
                videoUrl += String.format("?t=%s", (videoTime / 1000));
            }

            LogHelper.debug(VideoHelpers.class, "Video URL: " + videoUrl);

            setClipboard(ReVancedUtils.getContext(), videoUrl);

            Toast.makeText(ReVancedUtils.getContext(), str("share_copy_url_success"), Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            LogHelper.printException(VideoHelpers.class, "Couldn't generate video url", ex);
        }
    }

    private static void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("link", text);
            clipboard.setPrimaryClip(clip);
        }
    }
}
