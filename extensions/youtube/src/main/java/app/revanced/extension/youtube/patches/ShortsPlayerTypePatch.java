package app.revanced.extension.youtube.patches;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ShortsPlayerTypePatch {

    public enum ShortsPlayerType {
        SHORTS_PLAYER,
        REGULAR_PLAYER,
        REGULAR_PLAYER_FULLSCREEN
    }

    static {
        if (!VersionCheckPatch.IS_19_46_OR_GREATER
                && Settings.SHORTS_PLAYER_TYPE.get() == ShortsPlayerType.REGULAR_PLAYER_FULLSCREEN) {
            // User imported newer settings to an older app target.
            Logger.printInfo(() -> "Resetting " + Settings.SHORTS_PLAYER_TYPE);
            Settings.SHORTS_PLAYER_TYPE.resetToDefault();
        }
    }

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
            ShortsPlayerType type = Settings.SHORTS_PLAYER_TYPE.get();
            if (type == ShortsPlayerType.SHORTS_PLAYER) {
                return false; // Default unpatched behavior.
            }

            if (videoID.isEmpty()) {
                // Shorts was opened using launcher app shortcut.
                Logger.printDebug(() -> "Ignoring Short with no videoId");
                return false;
            }
            
            if (NavigationButton.getSelectedNavigationButton() == NavigationButton.SHORTS) {
                return false; // Always use Shorts player for the Shorts nav button.
            }

            final boolean forceFullScreen = (type == ShortsPlayerType.REGULAR_PLAYER_FULLSCREEN);
            OpenVideosFullscreenHookPatch.setOpenNextVideoFullscreen(forceFullScreen);

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
            OpenVideosFullscreenHookPatch.setOpenNextVideoFullscreen(null);
            Logger.printException(() -> "openShort failure", ex);
            return false;
        }
    }
}
