package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.shared.PlayerControlsVisibility;

@SuppressWarnings("unused")
public class PlayerControlsVisibilityHookPatch {

    /**
     * Injection point.
     */
    public static void setPlayerControlsVisibility(@Nullable Enum<?> youTubePlayerControlsVisibility) {
        if (youTubePlayerControlsVisibility == null) return;

        PlayerControlsVisibility.setFromString(youTubePlayerControlsVisibility.name());
    }
}
