package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.shared.PlayerType;
import app.revanced.extension.youtube.shared.VideoState;

@SuppressWarnings("unused")
public class PlayerTypeHookPatch {
    /**
     * Injection point.
     */
    public static void setPlayerType(@Nullable Enum<?> youTubePlayerType) {
        if (youTubePlayerType == null) return;

        PlayerType.setFromString(youTubePlayerType.name());
    }

    /**
     * Injection point.
     */
    public static void setVideoState(@Nullable Enum<?> youTubeVideoState) {
        if (youTubeVideoState == null) return;

        VideoState.setFromString(youTubeVideoState.name());
    }
}
