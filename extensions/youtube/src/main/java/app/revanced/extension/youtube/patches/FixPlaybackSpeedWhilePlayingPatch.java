package app.revanced.extension.youtube.patches;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class FixPlaybackSpeedWhilePlayingPatch {

    private static final float DEFAULT_YOUTUBE_PLAYBACK_SPEED = 1.0f;

    public static boolean playbackSpeedChanged(float playbackSpeed) {
        if (playbackSpeed == DEFAULT_YOUTUBE_PLAYBACK_SPEED &&
                PlayerType.getCurrent().isMaximizedOrFullscreen()) {

            Logger.printDebug(() -> "Blocking call to change playback speed to 1.0x");

            return true;
        }

        return false;
    }

}

