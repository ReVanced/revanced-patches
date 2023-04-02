package app.revanced.integrations.videoplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringRef;

public class DownloadButton extends BottomControlButton {
    public static DownloadButton instance;

    public DownloadButton(Object obj) {
        super(
                obj,
                "download_button",
                SettingsEnum.DOWNLOADS_BUTTON_SHOWN.getBoolean(),
                DownloadButton::onDownloadClick
        );
    }

    public static void initializeButton(Object obj) {
        instance = new DownloadButton(obj);
    }

    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }

    private static void onDownloadClick(View view) {
        LogHelper.printDebug(() -> "Download button clicked");

        final var context = view.getContext();
        var downloaderPackageName = SettingsEnum.DOWNLOADS_PACKAGE_NAME.getString();

        boolean packageEnabled = false;
        try {
            assert context != null;
            packageEnabled = context.getPackageManager().getApplicationInfo(downloaderPackageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException error) {
            LogHelper.printDebug(() -> "Downloader could not be found: " + error);
        }

        // If the package is not installed, show the toast
        if (!packageEnabled) {
            ReVancedUtils.showToastLong(downloaderPackageName + " " + StringRef.str("downloader_not_installed_warning"));
            return;
        }

        // Launch PowerTube intent
        try {
            String content = String.format("https://youtu.be/%s", VideoInformation.getCurrentVideoId());

            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.setPackage(downloaderPackageName);
            intent.putExtra("android.intent.extra.TEXT", content);
            context.startActivity(intent);

            LogHelper.printDebug(() -> "Launched the intent with the content: " + content);
        } catch (Exception error) {
            LogHelper.printException(() -> "Failed to launch the intent: " + error, error);
        }
    }
}

