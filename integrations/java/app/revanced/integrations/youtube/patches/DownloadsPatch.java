package app.revanced.integrations.youtube.patches;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.StringRef;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.integrations.youtube.settings.Settings;

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
     *
     * Call if download playlist is pressed, or if download button is used
     * for old spoofed version (both playlists and the player action button).
     *
     * Downloading playlists is not supported yet,
     * as the hooked code does not easily expose the playlist id.
     */
    public static boolean inAppDownloadPlaylistLegacyOnClick(@Nullable String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            // videoId is null or empty if download playlist is pressed.
            Logger.printDebug(() -> "Ignoring playlist download button press");
            return false;
        }
        return inAppDownloadButtonOnClick();
    }

    /**
     * Injection point.
     */
    public static boolean inAppDownloadButtonOnClick() {
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
                context = Utils.getContext();
                isActivityContext = false;
            }

            launchExternalDownloader(context, isActivityContext);
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
    public static void launchExternalDownloader(@NonNull Context context, boolean isActivityContext) {
        Logger.printDebug(() -> "Launching external downloader with context: " + context);

        // Trim string to avoid any accidental whitespace.
        var downloaderPackageName = Settings.EXTERNAL_DOWNLOADER_PACKAGE_NAME.get().trim();

        boolean packageEnabled = false;
        try {
            packageEnabled = context.getPackageManager().getApplicationInfo(downloaderPackageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException error) {
            Logger.printDebug(() -> "External downloader could not be found: " + error);
        }

        // If the package is not installed, show the toast
        if (!packageEnabled) {
            Utils.showToastLong(StringRef.str("revanced_external_downloader_not_installed_warning", downloaderPackageName));
            return;
        }

        // Launch intent
        try {
            String content = String.format("https://youtu.be/%s", VideoInformation.getVideoId());

            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.setPackage(downloaderPackageName);
            intent.putExtra("android.intent.extra.TEXT", content);
            if (!isActivityContext) {
                Logger.printDebug(() -> "Using new task intent");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception error) {
            Logger.printException(() -> "Failed to launch intent: " + error, error);
        }
    }
}
