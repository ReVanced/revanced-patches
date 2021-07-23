package pl.jakubweg;

import android.app.Activity;
import android.app.AlertDialog;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

import pl.jakubweg.requests.Requester;

import static pl.jakubweg.SponsorBlockSettings.DefaultBehaviour;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_COUNT_SKIPS;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_NEW_SEGMENT_ENABLED;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_UUID;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_KEY_VOTING_ENABLED;
import static pl.jakubweg.SponsorBlockSettings.PREFERENCES_NAME;
import static pl.jakubweg.SponsorBlockSettings.adjustNewSegmentMillis;
import static pl.jakubweg.SponsorBlockSettings.countSkips;
import static pl.jakubweg.SponsorBlockSettings.setSeenGuidelines;
import static pl.jakubweg.SponsorBlockSettings.showToastWhenSkippedAutomatically;
import static pl.jakubweg.SponsorBlockSettings.uuid;
import static pl.jakubweg.StringRef.str;

@SuppressWarnings({"unused", "deprecation"}) // injected
public class SponsorBlockPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###,###");
    public static final String SAVED_TEMPLATE = "%dh %.1f minutes";
    private final ArrayList<Preference> preferencesToDisableWhenSBDisabled = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFERENCES_NAME);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        final Activity context = this.getActivity();

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        SponsorBlockSettings.update(context);

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED);
            preference.setDefaultValue(SponsorBlockSettings.isSponsorBlockEnabled);
            preference.setChecked(SponsorBlockSettings.isSponsorBlockEnabled);
            preference.setTitle(str("enable_sb"));
            preference.setSummary(str("enable_sb_sum"));
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                final boolean value = (Boolean) newValue;
                enableCategoriesIfNeeded(value);
                return true;
            });
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_NEW_SEGMENT_ENABLED);
            preference.setDefaultValue(SponsorBlockSettings.isAddNewSegmentEnabled);
            preference.setChecked(SponsorBlockSettings.isAddNewSegmentEnabled);
            preference.setTitle(str("enable_segmadding"));
            preference.setSummary(str("enable_segmadding_sum"));
            preferencesToDisableWhenSBDisabled.add(preference);
            preference.setOnPreferenceChangeListener((preference12, o) -> {
                final boolean value = (Boolean) o;
                if (value && !SponsorBlockSettings.seenGuidelinesPopup) {
                    new AlertDialog.Builder(preference12.getContext())
                            .setTitle(str("sb_guidelines_popup_title"))
                            .setMessage(str("sb_guidelines_popup_content"))
                            .setNegativeButton(str("sb_guidelines_popup_already_read"), null)
                            .setPositiveButton(str("sb_guidelines_popup_open"), (dialogInterface, i) -> openGuidelines())
                            .show();
                }
                return true;
            });
        }

        {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setTitle(str("enable_voting"));
            preference.setSummary(str("enable_voting_sum"));
            preference.setKey(PREFERENCES_KEY_VOTING_ENABLED);
            preference.setDefaultValue(SponsorBlockSettings.isVotingEnabled);
            preference.setChecked(SponsorBlockSettings.isVotingEnabled);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        addGeneralCategory(context, preferenceScreen);
        addSegmentsCategory(context, preferenceScreen);
        addStatsCategory(context, preferenceScreen);
        addAboutCategory(context, preferenceScreen);

        enableCategoriesIfNeeded(SponsorBlockSettings.isSponsorBlockEnabled);
    }

    private void openGuidelines() {
        final Context context = getActivity();
        setSeenGuidelines(context);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/ajayyy/SponsorBlock/wiki/Guidelines"));
        context.startActivity(intent);
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
        category.setTitle(str("diff_segments"));

        String defaultValue = DefaultBehaviour.key;
        SponsorBlockSettings.SegmentBehaviour[] segmentBehaviours = SponsorBlockSettings.SegmentBehaviour.values();
        String[] entries = new String[segmentBehaviours.length];
        String[] entryValues = new String[segmentBehaviours.length];
        for (int i = 0, segmentBehavioursLength = segmentBehaviours.length; i < segmentBehavioursLength; i++) {
            SponsorBlockSettings.SegmentBehaviour behaviour = segmentBehaviours[i];
            entries[i] = behaviour.name.toString();
            entryValues[i] = behaviour.key;
        }

        for (SponsorBlockSettings.SegmentInfo segmentInfo : SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted()) {
            ListPreference preference = new ListPreference(context);
            preference.setTitle(segmentInfo.getTitleWithDot());
            preference.setSummary(segmentInfo.description.toString());
            preference.setKey(segmentInfo.key);
            preference.setDefaultValue(defaultValue);
            preference.setEntries(entries);
            preference.setEntryValues(entryValues);
            category.addPreference(preference);
        }

    }

    private void addStatsCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("stats"));
        preferencesToDisableWhenSBDisabled.add(category);

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);
            preference.setTitle(str("stats_loading"));

            Requester.retrieveUserStats(category, preference);
        }
    }

    private void addAboutCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("about"));

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(str("about_api"));
            preference.setSummary(str("about_api_sum"));
            preference.setOnPreferenceClickListener(preference1 -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://sponsor.ajay.app"));
                preference1.getContext().startActivity(i);
                return false;
            });
        }

        {
            Preference preference = new Preference(context);
            screen.addPreference(preference);
            preference.setTitle(str("about_madeby"));
        }

    }

    private void addGeneralCategory(final Context context, PreferenceScreen screen) {
        final PreferenceCategory category = new PreferenceCategory(context);
        preferencesToDisableWhenSBDisabled.add(category);
        screen.addPreference(category);
        category.setTitle(str("general"));

        {
            Preference preference = new Preference(context);
            preference.setTitle(str("sb_guidelines_preference_title"));
            preference.setSummary(str("sb_guidelines_preference_sum"));
            preference.setOnPreferenceClickListener(preference1 -> {
                openGuidelines();
                return false;
            });
            screen.addPreference(preference);
        }

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(str("general_skiptoast"));
            preference.setSummary(str("general_skiptoast_sum"));
            preference.setKey(PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP);
            preference.setDefaultValue(showToastWhenSkippedAutomatically);
            preference.setOnPreferenceClickListener(preference12 -> {
                Toast.makeText(preference12.getContext(), str("skipped_sponsor"), Toast.LENGTH_SHORT).show();
                return false;
            });
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(str("general_skipcount"));
            preference.setSummary(str("general_skipcount_sum"));
            preference.setKey(PREFERENCES_KEY_COUNT_SKIPS);
            preference.setDefaultValue(countSkips);
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            preference.setTitle(str("general_adjusting"));
            preference.setSummary(str("general_adjusting_sum"));
            preference.setKey(PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP);
            preference.setDefaultValue(String.valueOf(adjustNewSegmentMillis));
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            Preference preference = new EditTextPreference(context);
            preference.setTitle(str("general_uuid"));
            preference.setSummary(str("general_uuid_sum"));
            preference.setKey(PREFERENCES_KEY_UUID);
            preference.setDefaultValue(uuid);
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SponsorBlockSettings.update(getActivity());
    }
}
