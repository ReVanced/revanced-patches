package app.revanced.extension.tiktok.cleardisplay;

import app.revanced.extension.tiktok.settings.Settings;

@SuppressWarnings("unused")
public class RememberClearDisplayPatch {
    
    private static Boolean cachedState = null;

    public static boolean getClearDisplayState() {
        if (cachedState == null) {
            cachedState = Settings.CLEAR_DISPLAY.get();
        }
        return cachedState;
    }

    public static void rememberClearDisplayState(boolean newState) {
        if (cachedState != null && cachedState == newState) {
            return;
        }
        
        cachedState = newState;
        Settings.CLEAR_DISPLAY.save(newState);
    }
}