package app.revanced.integrations.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

@SuppressWarnings("deprecation")
public abstract class ConditionalPreferenceCategory extends PreferenceCategory {
    public ConditionalPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context);

        if (getSettingsStatus())  {
            screen.addPreference(this);
            addPreferences(context);
        }
    }

    public abstract boolean getSettingsStatus();

    public abstract void addPreferences(Context context);
}

