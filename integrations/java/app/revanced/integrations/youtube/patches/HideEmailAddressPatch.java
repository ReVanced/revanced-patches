package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideEmailAddressPatch {
    //Used by app.revanced.patches.youtube.layout.personalinformation.patch.HideEmailAddressPatch
    public static int hideEmailAddress(int originalValue) {
        if (Settings.HIDE_EMAIL_ADDRESS.get())
            return 8;
        return originalValue;
    }
}
