package app.revanced.extension.all.spoof.adb;

import android.content.ContentResolver;
import android.provider.Settings;
import java.util.Set;

@SuppressWarnings("unused")
public final class SpoofAdbPatch {
    private static final Set<String> SPOOF_SETTINGS = Set.of("adb_enabled", "adb_wifi_enabled", "development_settings_enabled");

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
