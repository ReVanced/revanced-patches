package app.revanced.integrations.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;

import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class IntegrationsPreferenceCategory extends ConditionalPreferenceCategory {
    public IntegrationsPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Integrations");
    }

    @Override
    public boolean getSettingsStatus() {
        return true;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new TogglePreference(context,
                "Enable debug log",
                "Show integration debug log.",
                BaseSettings.DEBUG
        ));
    }
}
