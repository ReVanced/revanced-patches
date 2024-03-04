package app.revanced.integrations.youtube.patches;

import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.StringRef;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

public final class DownloadsPatch {
    public static boolean inAppDownloadButtonOnClick() {
        if (!Settings.USE_IN_APP_DOWNLOAD_BUTTON.get())
            return false;

        launchExternalDownloader();
        return true;
    }

    public static void launchExternalDownloader() {
        Logger.printDebug(() -> "Launching external downloader");

        final var context = Utils.getContext();

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
            context.startActivity(intent);

            Logger.printDebug(() -> "Launched the intent with the content: " + content);
        } catch (Exception error) {
            Logger.printException(() -> "Failed to launch the intent: " + error, error);
        }
    }
}
