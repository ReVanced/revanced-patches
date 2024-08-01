package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.patches.DownloadsPatch;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ExternalDownloadButton extends BottomControlButton {
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
     * Injection point.
     */
    public static void changeVisibility(boolean showing) {
        if (instance != null) instance.setVisibility(showing);
    }

    private static void onDownloadClick(View view) {
        DownloadsPatch.launchExternalDownloader(
                VideoInformation.getVideoId(),
                view.getContext(),
                true);
    }
}

