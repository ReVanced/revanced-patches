package app.revanced.integrations.videoplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.StringRef;

public class ExternalDownloadButton extends BottomControlButton {
    @Nullable
    private static ExternalDownloadButton instance;

    public ExternalDownloadButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "external_download_button",
                SettingsEnum.EXTERNAL_DOWNLOADER,
                ExternalDownloadButton::onDownloadClick,
                null
        );
    }

    /**
     * Injection point.
     */
    public static void initializeButton(View view) {
        try {
            instance = new ExternalDownloadButton((ViewGroup) view);
        } catch (Exception ex) {
            LogHelper.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }

    private static void onDownloadClick(View view) {
        LogHelper.printDebug(() -> "External download button clicked");

        final var context = view.getContext();
        var downloaderPackageName = SettingsEnum.EXTERNAL_DOWNLOADER_PACKAGE_NAME.getString();

        boolean packageEnabled = false;
        try {
            packageEnabled = context.getPackageManager().getApplicationInfo(downloaderPackageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException error) {
            LogHelper.printDebug(() -> "External downloader could not be found: " + error);
        }

        // If the package is not installed, show the toast
        if (!packageEnabled) {
            ReVancedUtils.showToastLong(downloaderPackageName + " " + StringRef.str("external_downloader_not_installed_warning"));
            return;
        }

        // Launch PowerTube intent
        try {
            String content = String.format("https://youtu.be/%s", VideoInformation.getVideoId());

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

