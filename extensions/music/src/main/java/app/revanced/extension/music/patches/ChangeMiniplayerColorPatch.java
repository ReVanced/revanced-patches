package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class ChangeMiniplayerColorPatch {

    /**
     * Injection point
     */
    public static boolean changeMiniplayerColor() {
        return Settings.CHANGE_MINIPLAYER_COLOR.get();
    }
}
