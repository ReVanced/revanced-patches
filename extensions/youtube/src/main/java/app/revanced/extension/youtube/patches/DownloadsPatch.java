package app.revanced.extension.youtube.patches;

import static app.revanced.extension.youtube.settings.preference.ExternalDownloaderPreference.showDialogIfAppIsNotInstalled;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class DownloadsPatch {

    private static WeakReference<Activity> activityRef = new WeakReference<>(null);

    /**
     * Injection point.
     */
    public static void activityCreated(Activity mainActivity) {
        activityRef = new WeakReference<>(mainActivity);
    }

    /**
     * Injection point.
     * <p>
     * Called from the in app download hook,
     * for both the player action button (below the video)
     * and the 'Download video' flyout option for feed videos.
     * <p>
     * Appears to always be called from the main thread.
     */
    public static boolean inAppDownloadButtonOnClick(String videoId) {
        try {
            if (!Settings.EXTERNAL_DOWNLOADER_ACTION_BUTTON.get()) {
                return false;
            }

            // If possible, use the main activity as the context.
            // Otherwise fall back on using the application context.
            Context context = activityRef.get();
            boolean isActivityContext = true;
            if (context == null) {
                // Utils context is the application context, and not an activity context.
                //
                // Edit: This check may no longer be needed since YT can now
                // only be launched from the main Activity (embedded usage in other apps no longer works).
                context = Utils.getContext();
                isActivityContext = false;
            }

            launchExternalDownloader(videoId, context, isActivityContext);
            return true;
        } catch (Exception ex) {
            Logger.printException(() -> "inAppDownloadButtonOnClick failure", ex);
        }
        return false;
    }

    /**
     * @param isActivityContext If the context parameter is for an Activity.  If this is false, then
     *                          the downloader is opened as a new task (which forces YT to minimize).
     */
    public static void launchExternalDownloader(String videoId, Context context, boolean isActivityContext) {
        try {
            Objects.requireNonNull(videoId);
            Logger.printDebug(() -> "Launching external downloader with context: " + context);

            // Trim string to avoid any accidental whitespace.
            var downloaderPackageName = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.get().trim();

            // If the package is not installed, show a dialog.
            if (showDialogIfAppIsNotInstalled(context, downloaderPackageName)) {
                return;
            }

            String content = "https://youtu.be/" + videoId;
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.setPackage(downloaderPackageName);
            intent.putExtra("android.intent.extra.TEXT", content);
            if (!isActivityContext) {
                Logger.printDebug(() -> "Using new task intent");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception ex) {
            Logger.printException(() -> "launchExternalDownloader failure", ex);
        }
    }
}
