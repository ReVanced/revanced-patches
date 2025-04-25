package app.revanced.extension.all.misc.hide.adb;

import android.content.ContentResolver;
import android.provider.Settings;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public final class HideAdbPatch {
    private static final List<String> SPOOF_SETTINGS = Arrays.asList("adb_enabled", "adb_wifi_enabled", "development_settings_enabled");

    public static int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        if (SPOOF_SETTINGS.contains(name)) {
            return 0;
        }

        return Settings.Global.getInt(cr, name);
    }

    public static int getInt(ContentResolver cr, String name, int def) {
        if (SPOOF_SETTINGS.contains(name)) {
            return 0;
        }

        return Settings.Global.getInt(cr, name, def);
    }
}
