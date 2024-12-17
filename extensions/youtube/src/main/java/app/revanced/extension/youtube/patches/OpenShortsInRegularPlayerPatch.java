package app.revanced.extension.youtube.patches;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OpenShortsInRegularPlayerPatch {

    private static WeakReference<Activity> mainActivityRef = new WeakReference<>(null);

    /**
     * Injection point.
     */
    public static void setMainActivity(Activity activity) {
        mainActivityRef = new WeakReference<>(activity);
    }

    /**
     * Injection point.
     */
    public static boolean openShort(String videoID) {
        try {
            if (!Settings.OPEN_SHORTS_IN_REGULAR_PLAYER.get()) {
                return false;
            }
            
            if (NavigationButton.getSelectedNavigationButton() == NavigationButton.SHORTS) {
                return false;
            }

            // Can use the application context and add intent flags of
            // FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TOP
            // But the activity context seems to fix random app crashes
            // if Shorts urls are opened outside the app.
            var context = mainActivityRef.get();

             Intent videoPlayerIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://youtube.com/watch?v=" + videoID)
            );

            videoPlayerIntent.setPackage(context.getPackageName());

            context.startActivity(videoPlayerIntent);
            return true;
        } catch (Exception ex) {
            Logger.printException(() -> "openShort failure", ex);
            return false;
        }
    }
}
