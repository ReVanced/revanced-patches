package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

@SuppressWarnings("unused")
public class DisableAutoCaptionsPatch {

    /**
     * Used by injected code. Do not delete.
     */
    public static boolean captionsButtonDisabled;

    public static boolean autoCaptionsEnabled() {
        return Settings.AUTO_CAPTIONS.get()
                // Do not use auto captions for Shorts.
                && ShortsPlayerState.isOpen();
    }

}
