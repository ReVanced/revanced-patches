package pl.jakubweg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static pl.jakubweg.Helper.getStringByName;
import static pl.jakubweg.SponsorBlockSettings.DefaultBehaviour;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_CACHE_SEGMENTS;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_COUNT_SKIPS;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_NEW_SEGMENT_ENABLED;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_UUID;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_NAME;
import static pl.jakubweg.SponsorBlockSettings.adjustNewSegmentMillis;
import static pl.jakubweg.SponsorBlockSettings.cacheEnabled;
import static pl.jakubweg.SponsorBlockSettings.countSkips;
import static pl.jakubweg.SponsorBlockSettings.showToastWhenSkippedAutomatically;
import static pl.jakubweg.SponsorBlockSettings.uuid;

public class SponsorBlockPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ArrayList<Preference> preferencesToDisableWhenSBDisabled = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        Activity context = this.getActivity();

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED);
            preference.setDefaultValue(SponsorBlockSettings.isSponsorBlockEnabled);
            preference.setChecked(SponsorBlockSettings.isSponsorBlockEnabled);
            preference.setTitle(getStringByName(context, "enable_sb"));
            preference.setSummary(getStringByName(context, "enable_sb_sum"));
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    enableCategoriesIfNeeded(((Boolean) newValue));
                    return true;
                }
            });
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_NEW_SEGMENT_ENABLED);
            preference.setDefaultValue(SponsorBlockSettings.isAddNewSegmentEnabled);
            preference.setChecked(SponsorBlockSettings.isAddNewSegmentEnabled);
            preference.setTitle(getStringByName(context, "enable_segmadding"));
            preference.setSummary(getStringByName(context, "enable_segmadding_sum"));
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        addGeneralCategory(context, preferenceScreen);
        addSegmentsCategory(context, preferenceScreen);
        addAboutCategory(context, preferenceScreen);

        enableCategoriesIfNeeded(SponsorBlockSettings.isSponsorBlockEnabled);
    }

    private void enableCategoriesIfNeeded(boolean enabled) {
        for (Preference preference : preferencesToDisableWhenSBDisabled)
            preference.setEnabled(enabled);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void addSegmentsCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        preferencesToDisableWhenSBDisabled.add(category);
        category.setTitle(getStringByName(context, "diff_segments"));

        String defaultValue = DefaultBehaviour.key;
        SponsorBlockSettings.SegmentBehaviour[] segmentBehaviours = SponsorBlockSettings.SegmentBehaviour.values();
        String[] entries = new String[segmentBehaviours.length];
        String[] entryValues = new String[segmentBehaviours.length];
        for (int i = 0, segmentBehavioursLength = segmentBehaviours.length; i < segmentBehavioursLength; i++) {
            SponsorBlockSettings.SegmentBehaviour behaviour = segmentBehaviours[i];
            entries[i] = behaviour.name;
            entryValues[i] = behaviour.key;
        }

        for (SponsorBlockSettings.SegmentInfo segmentInfo : SponsorBlockSettings.SegmentInfo.valuesWithoutPreview()) {
            ListPreference preference = new ListPreference(context);
            preference.setTitle(segmentInfo.getTitleWithDot());
            preference.setSummary(segmentInfo.description);
            preference.setKey(segmentInfo.key);
            preference.setDefaultValue(defaultValue);
            preference.setEntries(entries);
            preference.setEntryValues(entryValues);
            category.addPreference(preference);
        }

    }

    private void addAboutCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle("About");

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(getStringByName(context, "about_api"));
            preference.setSummary(getStringByName(context, "about_api_sum"));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("http://sponsor.ajay.app"));
                    preference.getContext().startActivity(i);
                    return false;
                }
            });
        }

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(getStringByName(context, "about_madeby"));
        }

    }

    private void addGeneralCategory(final Context context, PreferenceScreen screen) {
        final PreferenceCategory category = new PreferenceCategory(context);
        preferencesToDisableWhenSBDisabled.add(category);
        screen.addPreference(category);
        category.setTitle(getStringByName(context, "general"));

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(getStringByName(context, "general_skiptoast"));
            preference.setSummary(getStringByName(context, "general_skiptoast_sum"));
            preference.setKey(PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP);
            preference.setDefaultValue(showToastWhenSkippedAutomatically);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(preference.getContext(), getStringByName(context, "skipped_segment"), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(getStringByName(context, "general_skipcount"));
            preference.setSummary(getStringByName(context, "general_skipcount_sum"));
            preference.setKey(PREFERENCES_KEY_COUNT_SKIPS);
            preference.setDefaultValue(countSkips);
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            preference.setTitle(getStringByName(context, "general_adjusting"));
            preference.setSummary(getStringByName(context, "general_adjusting_sum"));
            preference.setKey(PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP);
            preference.setDefaultValue(String.valueOf(adjustNewSegmentMillis));
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            Preference preference = new EditTextPreference(context);
            preference.setTitle(getStringByName(context, "general_uuid"));
            preference.setSummary(getStringByName(context, "general_uuid_sum"));
            preference.setKey(PREFERENCES_KEY_UUID);
            preference.setDefaultValue(uuid);
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(getStringByName(context, "general_cache"));
            preference.setSummary(getStringByName(context, "general_cache_sum"));
            preference.setKey(PREFERENCES_KEY_CACHE_SEGMENTS);
            preference.setDefaultValue(cacheEnabled);
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            Preference preference = new Preference(context);
            preference.setTitle(getStringByName(context, "general_cache_clear"));
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    File cacheDirectory = SponsorBlockSettings.cacheDirectory;
                    if (cacheDirectory != null) {
                        for (File file : cacheDirectory.listFiles()) {
                            if (!file.delete())
                                return false;
                        }
                        Toast.makeText(getActivity(), getStringByName(context, "done"), Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SponsorBlockSettings.update(getActivity());
    }
}
