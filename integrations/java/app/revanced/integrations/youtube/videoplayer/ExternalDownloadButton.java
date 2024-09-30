package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.patches.DownloadsPatch;
import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ExternalDownloadButton extends PlayerControlButton {
    @Nullable
    private static ExternalDownloadButton instance;

    public ExternalDownloadButton(ViewGroup viewGroup) {
        super(
                viewGroup,
                "revanced_external_download_button",
                Settings.EXTERNAL_DOWNLOADER,
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
            Logger.printException(() -> "initializeButton failure", ex);
        }
    }

    /**
     * injection point
     */
    public static void changeVisibilityImmediate(boolean visible) {
        if (instance != null) instance.setVisibilityImmediate(visible);
    }

    /**
     * injection point
     */
    public static void changeVisibility(boolean visible, boolean animated) {
        if (instance != null) instance.setVisibility(visible, animated);
    }

    private static void onDownloadClick(View view) {
        DownloadsPatch.launchExternalDownloader(
                VideoInformation.getVideoId(),
                view.getContext(),
                true);
    }
}

