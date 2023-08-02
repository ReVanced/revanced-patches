package app.revanced.tiktok.settingsmenu.preference;

import android.content.Context;
import android.preference.EditTextPreference;

import app.revanced.tiktok.settings.SettingsEnum;

public class InputTextPreference extends EditTextPreference {

    public InputTextPreference(Context context, String title, String summary, SettingsEnum setting) {
        super(context);
        this.setTitle(title);
        this.setSummary(summary);
        this.setKey(setting.path);
        this.setText(setting.getString());
    }
}
