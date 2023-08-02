package app.revanced.tiktok.settingsmenu.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.tiktok.settings.SettingsEnum;
import app.revanced.tiktok.settingsmenu.preference.TogglePreference;

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
                SettingsEnum.DEBUG
        ));
    }
}
