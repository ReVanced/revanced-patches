package app.revanced.extension.tiktok.cleardisplay;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.tiktok.settings.Settings;

@SuppressWarnings("unused")
public class RememberClearDisplayPatch {
    private static volatile Boolean lastLoggedState;

    public static boolean getClearDisplayState() {
        boolean state = Settings.CLEAR_DISPLAY.get();
        if (BaseSettings.DEBUG.get() && (lastLoggedState == null || lastLoggedState != state)) {
            lastLoggedState = state;
            Logger.printInfo(() -> "[ReVanced ClearDisplay] get state=" + state);
        }
        return state;
    }
    public static void rememberClearDisplayState(boolean newState) {
        if (BaseSettings.DEBUG.get()) {
            boolean oldState = Settings.CLEAR_DISPLAY.get();
            Logger.printInfo(() -> "[ReVanced ClearDisplay] remember state " + oldState + " -> " + newState);
        }
        Settings.CLEAR_DISPLAY.save(newState);
    }
}
