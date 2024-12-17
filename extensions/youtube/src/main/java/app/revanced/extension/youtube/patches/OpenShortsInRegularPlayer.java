package app.revanced.extension.youtube.patches;

import android.content.Intent;
import android.net.Uri;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OpenShortsInRegularPlayer {

    /**
     * Injection point.
     */
    public static boolean openShorts(String shortsVideoID) {
        try {
            if (!Settings.OPEN_SHORTS_IN_REGULAR_PLAYER.get()) {
                return false;
            }

            var context = Utils.getContext();

            Intent videoPlayerIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + shortsVideoID)
            );
            videoPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            videoPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            videoPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            videoPlayerIntent.setPackage(context.getPackageName());

            context.startActivity(videoPlayerIntent);
            return true;
        } catch (Exception ex) {
            Logger.printException(() -> "openShorts failure", ex);
            return false;
        }
    }
}
