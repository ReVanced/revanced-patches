package app.revanced.extension.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.tiktok.settings.preference.ReVancedTikTokAboutPreference;
import app.revanced.extension.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class ExtensionPreferenceCategory extends ConditionalPreferenceCategory {
    public ExtensionPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Miscellaneous");
    }

    @Override
    public boolean getSettingsStatus() {
        return true;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new ReVancedTikTokAboutPreference(context));

        addPreference(new TogglePreference(context,
                "Sanitize sharing links",
                "Remove tracking parameters from shared links.",
                BaseSettings.SANITIZE_SHARED_LINKS
        ));

        addPreference(new TogglePreference(context,
                "Enable debug log",
                "Show extension debug log.",
                BaseSettings.DEBUG
        ));
    }
}
