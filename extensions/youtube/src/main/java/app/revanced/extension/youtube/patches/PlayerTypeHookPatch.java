package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.shared.PlayerType;
import app.revanced.extension.youtube.shared.VideoState;
import app.revanced.extension.youtube.sponsorblock.ui.CreateSegmentButton;
import app.revanced.extension.youtube.sponsorblock.ui.VotingButton;
import app.revanced.extension.youtube.videoplayer.CopyVideoUrlButton;
import app.revanced.extension.youtube.videoplayer.CopyVideoUrlTimestampButton;
import app.revanced.extension.youtube.videoplayer.ExternalDownloadButton;
import app.revanced.extension.youtube.videoplayer.PlaybackSpeedDialogButton;

@SuppressWarnings("unused")
public class PlayerTypeHookPatch {
    /**
     * Injection point.
     */
    public static void setPlayerType(@Nullable Enum<?> youTubePlayerType) {
        if (youTubePlayerType == null) return;

        PlayerType.setFromString(youTubePlayerType.name());

        // Synchronize the visibility of custom player buttons whenever the player type changes.
        // This ensures that button animations are cleared and their states are updated correctly
        // when switching between states like minimized, maximized, or fullscreen, preventing
        // "stuck" animations or incorrect visibility.
        CopyVideoUrlButton.onPlayerTypeChanged(PlayerType.getCurrent());
        CopyVideoUrlTimestampButton.onPlayerTypeChanged(PlayerType.getCurrent());
        ExternalDownloadButton.onPlayerTypeChanged(PlayerType.getCurrent());
        PlaybackSpeedDialogButton.onPlayerTypeChanged(PlayerType.getCurrent());
        CreateSegmentButton.onPlayerTypeChanged(PlayerType.getCurrent());
        VotingButton.onPlayerTypeChanged(PlayerType.getCurrent());
    }

    /**
     * Injection point.
     */
    public static void setVideoState(@Nullable Enum<?> youTubeVideoState) {
        if (youTubeVideoState == null) return;

        VideoState.setFromString(youTubeVideoState.name());
    }
}
