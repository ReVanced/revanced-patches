package fi.razerman.youtube.preferences;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import fi.razerman.youtube.Helpers.SharedPrefs;

/* loaded from: classes6.dex */
public class BooleanPreferences {
    public static boolean isTapSeekingEnabled() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_enable_tap_seeking", true);
    }

    public static boolean isExoplayerV2Enabled() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_exoplayer_v2", true);
    }

    public static boolean isCreateButtonHidden() {
        return SharedPrefs.getBoolean(Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()), "xfile_create_button_hidden", false);
    }
}
