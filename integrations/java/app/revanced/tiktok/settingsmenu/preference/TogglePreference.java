package app.revanced.tiktok.settingsmenu.preference;

import android.content.Context;
import android.preference.SwitchPreference;

import app.revanced.tiktok.settings.SettingsEnum;

@SuppressWarnings("deprecation")
public class TogglePreference extends SwitchPreference {
    public TogglePreference(Context context, String title, String summary, SettingsEnum setting) {
        super(context);
        this.setTitle(title);
        this.setSummary(summary);
        this.setKey(setting.path);
        this.setChecked(setting.getBoolean());
    }
}
