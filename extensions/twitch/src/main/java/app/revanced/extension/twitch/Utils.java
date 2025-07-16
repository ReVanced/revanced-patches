package app.revanced.extension.twitch;

import app.revanced.extension.shared.ResourceType;

public class Utils {

    /* Called from SettingsPatch smali */
    public static int getStringId(String name) {
        return app.revanced.extension.shared.Utils.getResourceIdentifier(
                ResourceType.STRING, name);
    }

    /* Called from SettingsPatch smali */
    public static int getDrawableId(String name) {
        return app.revanced.extension.shared.Utils.getResourceIdentifier(
                ResourceType.DRAWABLE, name);
    }
}
