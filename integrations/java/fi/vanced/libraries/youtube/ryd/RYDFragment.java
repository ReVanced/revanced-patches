package fi.vanced.libraries.youtube.ryd;

import static fi.razerman.youtube.XGlobals.debug;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_RYD_ENABLED;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_KEY_RYD_HINT_SHOWN;
import static fi.vanced.libraries.youtube.ryd.RYDSettings.PREFERENCES_NAME;
import static pl.jakubweg.StringRef.str;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import fi.vanced.utils.SharedPrefUtils;

public class RYDFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);

        final Activity context = this.getActivity();

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        // RYD enable toggle
        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_RYD_ENABLED);
            preference.setDefaultValue(false);
            preference.setChecked(SharedPrefUtils.getBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_ENABLED));
            preference.setTitle(str("vanced_ryd_title"));
            preference.setSummary(str("vanced_ryd_summary"));
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                final boolean value = (Boolean) newValue;
                ReturnYouTubeDislikes.onEnabledChange(value);
                return true;
            });
        }

        // Clear hint
        if (debug) {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_RYD_HINT_SHOWN);
            preference.setDefaultValue(false);
            preference.setChecked(SharedPrefUtils.getBoolean(context, PREFERENCES_NAME, PREFERENCES_KEY_RYD_HINT_SHOWN));
            preference.setTitle("Hint debug");
            preference.setSummary("Debug toggle for clearing the hint shown preference");
            preference.setOnPreferenceChangeListener((pref, newValue) -> true);
        }

        // About category
        addAboutCategory(context, preferenceScreen);
    }

    private void addAboutCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("about"));

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(str("vanced_ryd_attribution_title"));
            preference.setSummary(str("vanced_ryd_attribution_summary"));
            preference.setOnPreferenceClickListener(pref -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://returnyoutubedislike.com"));
                pref.getContext().startActivity(i);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle("GitHub");
            preference.setOnPreferenceClickListener(pref -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/Anarios/return-youtube-dislike"));
                pref.getContext().startActivity(i);
                return false;
            });
        }
    }
}
