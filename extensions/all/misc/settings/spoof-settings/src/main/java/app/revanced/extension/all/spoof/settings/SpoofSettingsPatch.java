package app.revanced.extension.all.spoof.settings;

import android.content.ContentResolver;

import android.provider.Settings;

@SuppressWarnings("unused")
public final class SpoofSettingsPatch {
    public static int getInt(ContentResolver cr, String name, int def) {
        if (name.equals("adb_enabled")) {
            return 0;
        }

        if(name.equals("development_settings_enabled")) {
            return 0;
        }

        return Settings.Global.getInt(cr, name, def);
    }
}
