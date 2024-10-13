package app.revanced.extension.twitch;

public class Utils {

    /* Called from SettingsPatch smali */
    public static int getStringId(String name) {
        return app.revanced.extension.shared.Utils.getResourceIdentifier(name, "string");
    }

    /* Called from SettingsPatch smali */
    public static int getDrawableId(String name) {
        return app.revanced.extension.shared.Utils.getResourceIdentifier(name, "drawable");
    }
}
