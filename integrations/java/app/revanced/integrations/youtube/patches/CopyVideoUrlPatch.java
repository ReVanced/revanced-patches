package app.revanced.integrations.youtube.patches;

import static app.revanced.integrations.shared.StringRef.str;

import android.os.Build;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

public class CopyVideoUrlPatch {

    public static void copyUrl(boolean withTimestamp) {
        try {
            StringBuilder builder = new StringBuilder("https://youtu.be/");
            builder.append(VideoInformation.getVideoId());
            final long currentVideoTimeInSeconds = VideoInformation.getVideoTime() / 1000;
            if (withTimestamp && currentVideoTimeInSeconds > 0) {
                final long hour = currentVideoTimeInSeconds / (60 * 60);
                final long minute = (currentVideoTimeInSeconds / 60) % 60;
                final long second = currentVideoTimeInSeconds % 60;
                builder.append("?t=");
                if (hour > 0) {
                    builder.append(hour).append("h");
                }
                if (minute > 0) {
                    builder.append(minute).append("m");
                }
                if (second > 0) {
                    builder.append(second).append("s");
                }
            }

            Utils.setClipboard(builder.toString());
            // Do not show a toast if using Android 13+ as it shows it's own toast.
            // But if the user copied with a timestamp then show a toast.
            // Unfortunately this will show 2 toasts on Android 13+, but no way around this.
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 || (withTimestamp && currentVideoTimeInSeconds > 0)) {
                Utils.showToastShort(withTimestamp && currentVideoTimeInSeconds > 0
                        ? str("revanced_share_copy_url_timestamp_success")
                        : str("revanced_share_copy_url_success"));
            }
        } catch (Exception e) {
            Logger.printException(() -> "Failed to generate video url", e);
        }
    }

}
