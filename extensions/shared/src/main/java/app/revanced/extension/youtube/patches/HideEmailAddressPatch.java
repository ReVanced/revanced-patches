package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

/**
 * Patch is obsolete and will be deleted in a future release
 */
@SuppressWarnings("unused")
@Deprecated()
public class HideEmailAddressPatch {
    //Used by app.revanced.patches.youtube.layout.personalinformation.patch.HideEmailAddressPatch
    public static int hideEmailAddress(int originalValue) {
        if (Settings.HIDE_EMAIL_ADDRESS.get())
            return 8;
        return originalValue;
    }
}
