package app.revanced.integrations.twitch;

public class Utils {

    /* Called from SettingsPatch smali */
    public static int getStringId(String name) {
        return app.revanced.integrations.shared.Utils.getResourceIdentifier(name, "string");
    }

    /* Called from SettingsPatch smali */
    public static int getDrawableId(String name) {
        return app.revanced.integrations.shared.Utils.getResourceIdentifier(name, "drawable");
    }
}
