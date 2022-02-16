package fi.vanced.libraries.youtube.player;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import static fi.razerman.youtube.XGlobals.debug;
import static pl.jakubweg.StringRef.str;

public class VideoHelpers {
    public static final String TAG = "VideoHelpers";

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
                if (debug) {
                    Log.d(TAG, "VideoId was empty");
                }
                return;
            }

            String videoUrl = String.format("https://youtu.be/%s", videoId);
            if (appendTimeStamp) {
                long videoTime = VideoInformation.lastKnownVideoTime;
                videoUrl += String.format("?t=%s", (videoTime / 1000));
            }

            if (debug) {
                Log.d(TAG, "Video URL: " + videoUrl);
            }

            setClipboard(YouTubeTikTokRoot_Application.getAppContext(), videoUrl);

            Toast.makeText(YouTubeTikTokRoot_Application.getAppContext(), str("share_copy_url_success"), Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex) {
            Log.e(TAG, "Couldn't generate video url", ex);
        }
    }

    private static void setClipboard(Context context, String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("link", text);
            clipboard.setPrimaryClip(clip);
        }
    }
}
