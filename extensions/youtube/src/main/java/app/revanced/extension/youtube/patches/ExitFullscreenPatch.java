package app.revanced.extension.youtube.patches;

import android.widget.ImageView;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class ExitFullscreenPatch {

    public enum FullscreenMode {
        DISABLED,
        PORTRAIT,
        LANDSCAPE,
        PORTRAIT_LANDSCAPE,
    }

    /**
     * Injection point.
     */
    public static void endOfVideoReached() {
        try {
            FullscreenMode mode = Settings.EXIT_FULLSCREEN.get();
            if (mode == FullscreenMode.DISABLED) {
                return;
            }

            if (PlayerType.getCurrent() == PlayerType.WATCH_WHILE_FULLSCREEN) {
                if (mode != FullscreenMode.PORTRAIT_LANDSCAPE) {
                    if (Utils.isLandscapeOrientation()) {
                        if (mode == FullscreenMode.PORTRAIT) {
                            return;
                        }
                    } else if (mode == FullscreenMode.LANDSCAPE) {
                        return;
                    }
                }

                // If the user cold launches the app and plays a video but does not
                // tap to show the overlay controls, the fullscreen button is not
                // set because the overlay controls are not attached.
                // To fix this, push the perform click to the back fo the main thread,
                // and by then the overlay controls will be visible since the video is now finished.
                Utils.runOnMainThread(() -> {
                    ImageView button = PlayerControlsPatch.fullscreenButtonRef.get();
                    if (button == null) {
                        Logger.printDebug(() -> "Fullscreen button is null, cannot click");
                    } else {
                        Logger.printDebug(() -> "Clicking fullscreen button");
                        final boolean soundEffectsEnabled = button.isSoundEffectsEnabled();
                        button.setSoundEffectsEnabled(false);
                        button.performClick();
                        button.setSoundEffectsEnabled(soundEffectsEnabled);
                    }
                });
            }
        } catch (Exception ex) {
            Logger.printException(() -> "endOfVideoReached failure", ex);
        }
    }
}
