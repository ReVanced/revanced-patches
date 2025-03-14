package app.revanced.extension.all.hide.adb;

import android.content.ContentResolver;
import android.provider.Settings;

import java.util.Set;

@SuppressWarnings("unused")
public final class HideAdbPatch {
    private static final String[] SPOOF_SETTINGS = new String[]{
            "adb_enabled",
            "adb_wifi_enabled",
            "development_settings_enabled"
    };

    private static boolean shouldSpoof(String name) {
        for (String spoofSetting : SPOOF_SETTINGS) {
            if (!spoofSetting.equals(name)) continue;
            return true;
        }

        return false;
    }

    public static int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        if (shouldSpoof(name)) {
            return 0;
        }

        return Settings.Global.getInt(cr, name);
    }

    public static int getInt(ContentResolver cr, String name, int def) {
        if (shouldSpoof(name)) {
            return 0;
        }

        return Settings.Global.getInt(cr, name, def);
    }
}
