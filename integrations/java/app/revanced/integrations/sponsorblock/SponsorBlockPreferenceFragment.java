package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.DEFAULT_API_URL;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_API_URL;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_BROWSER_BUTTON;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_COUNT_SKIPS;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_MIN_DURATION;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_NEW_SEGMENT_ENABLED;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_SHOW_TIME_WITHOUT_SEGMENTS;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_SHOW_TOAST_WHEN_SKIP;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_ENABLED;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_UUID;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.PREFERENCES_KEY_VOTING_ENABLED;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.adjustNewSegmentMillis;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.countSkips;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.minDuration;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.setSeenGuidelines;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.showTimeWithoutSegments;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.showToastWhenSkippedAutomatically;
import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.uuid;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;

import app.revanced.integrations.adremover.whitelist.WhitelistType;
import app.revanced.integrations.settings.Settings;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.sponsorblock.objects.EditTextListPreference;
import app.revanced.integrations.sponsorblock.requests.SBRequester;

@SuppressWarnings({"unused", "deprecation"}) // injected
public class SponsorBlockPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###,###");
    public static final String SAVED_TEMPLATE = "%dh %.1f %s";
    private static final APIURLChangeListener API_URL_CHANGE_LISTENER = new APIURLChangeListener();
    private final ArrayList<Preference> preferencesToDisableWhenSBDisabled = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK.getName());

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

        // Clear hint
        if (SettingsEnum.DEBUG_BOOLEAN.getBoolean()) {
            SwitchPreference preference = new SwitchPreference(context);
            preferenceScreen.addPreference(preference);
            preference.setKey(PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN);
            preference.setDefaultValue(false);
            preference.setChecked(SharedPrefHelper.getBoolean(context, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK, PREFERENCES_KEY_SPONSOR_BLOCK_HINT_SHOWN));
            preference.setTitle("Hint debug");
            preference.setSummary("Debug toggle for clearing the hint shown preference");
            preference.setOnPreferenceChangeListener((pref, newValue) -> true);
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
        intent.setData(Uri.parse("https://wiki.sponsor.ajay.app/w/Guidelines"));
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

        SponsorBlockSettings.SegmentBehaviour[] segmentBehaviours = SponsorBlockSettings.SegmentBehaviour.values();
        String[] entries = new String[segmentBehaviours.length];
        String[] entryValues = new String[segmentBehaviours.length];
        for (int i = 0, segmentBehavioursLength = segmentBehaviours.length; i < segmentBehavioursLength; i++) {
            SponsorBlockSettings.SegmentBehaviour behaviour = segmentBehaviours[i];
            entries[i] = behaviour.name.toString();
            entryValues[i] = behaviour.key;
        }

        SponsorBlockSettings.SegmentInfo[] categories = SponsorBlockSettings.SegmentInfo.valuesWithoutUnsubmitted();

        for (SponsorBlockSettings.SegmentInfo segmentInfo : categories) {
            EditTextListPreference preference = new EditTextListPreference(context);
            preference.setTitle(segmentInfo.getTitleWithDot());
            preference.setSummary(segmentInfo.description.toString());
            preference.setKey(segmentInfo.key);
            preference.setDefaultValue(segmentInfo.behaviour.key);
            preference.setEntries(entries);
            preference.setEntryValues(entryValues);

            category.addPreference(preference);
        }

        Preference colorPreference = new Preference(context); // TODO remove this after the next major update
        screen.addPreference(colorPreference);
        colorPreference.setTitle(str("color_change"));
        colorPreference.setSummary(str("color_change_sum"));
        preferencesToDisableWhenSBDisabled.add(colorPreference);
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

            SBRequester.retrieveUserStats(category, preference);
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
            Preference preference = new SwitchPreference(context);
            preference.setTitle(str("general_time_without_sb"));
            preference.setSummary(str("general_time_without_sb_sum"));
            preference.setKey(PREFERENCES_KEY_SHOW_TIME_WITHOUT_SEGMENTS);
            preference.setDefaultValue(showTimeWithoutSegments);
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(str("general_whitelisting"));
            preference.setSummary(str("general_whitelisting_sum"));
            preference.setKey(WhitelistType.SPONSORBLOCK.getPreferenceEnabledName());
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            Preference preference = new SwitchPreference(context);
            preference.setTitle(str("general_browser_button"));
            preference.setSummary(str("general_browser_button_sum"));
            preference.setKey(PREFERENCES_KEY_BROWSER_BUTTON);
            preferencesToDisableWhenSBDisabled.add(preference);
            screen.addPreference(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            preference.setTitle(str("general_adjusting"));
            preference.setSummary(str("general_adjusting_sum"));
            preference.setKey(PREFERENCES_KEY_ADJUST_NEW_SEGMENT_STEP);
            preference.setDefaultValue(String.valueOf(adjustNewSegmentMillis));
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            preference.setTitle(str("general_min_duration"));
            preference.setSummary(str("general_min_duration_sum"));
            preference.setKey(PREFERENCES_KEY_MIN_DURATION);
            preference.setDefaultValue(String.valueOf(minDuration));
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

        {
            Preference preference = new Preference(context);
            String title = str("general_api_url");
            preference.setTitle(title);
            preference.setSummary(Html.fromHtml(str("general_api_url_sum")));
            preference.setOnPreferenceClickListener(preference1 -> {
                EditText editText = new EditText(context);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                editText.setText(SponsorBlockSettings.apiUrl);

                API_URL_CHANGE_LISTENER.setEditTextRef(editText);
                new AlertDialog.Builder(context)
                        .setTitle(title)
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton(str("reset"), API_URL_CHANGE_LISTENER)
                        .setPositiveButton(android.R.string.ok, API_URL_CHANGE_LISTENER)
                        .show();
                return true;
            });

            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }

        {
            EditTextPreference preference = new EditTextPreference(context);
            Context applicationContext = context.getApplicationContext();

            preference.setTitle(str("settings_ie"));
            preference.setSummary(str("settings_ie_sum"));
            preference.setText(SponsorBlockUtils.exportSettings(applicationContext));
            preference.setOnPreferenceChangeListener((preference1, newValue) -> {
                SponsorBlockUtils.importSettings((String) newValue, applicationContext);
                return false;
            });
            screen.addPreference(preference);
            preferencesToDisableWhenSBDisabled.add(preference);
        }
    }

    private static class APIURLChangeListener implements DialogInterface.OnClickListener {
        private WeakReference<EditText> editTextRef;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            EditText editText = editTextRef.get();
            if (editText == null)
                return;
            Context context = ((AlertDialog) dialog).getContext();
            Context applicationContext = context.getApplicationContext();
            SharedPreferences preferences = SharedPrefHelper.getPreferences(context, SharedPrefHelper.SharedPrefNames.SPONSOR_BLOCK);

            switch (which) {
                case DialogInterface.BUTTON_NEUTRAL:
                    preferences.edit().putString(PREFERENCES_KEY_API_URL, DEFAULT_API_URL).apply();
                    Toast.makeText(applicationContext, str("api_url_reset"), Toast.LENGTH_SHORT).show();
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    Editable text = editText.getText();
                    Toast invalidToast = Toast.makeText(applicationContext, str("api_url_invalid"), Toast.LENGTH_SHORT);
                    if (text == null) {
                        invalidToast.show();
                    } else {
                        String textAsString = text.toString();
                        if (textAsString.isEmpty() || !Patterns.WEB_URL.matcher(textAsString).matches()) {
                            invalidToast.show();
                        } else {
                            preferences.edit().putString(PREFERENCES_KEY_API_URL, textAsString).apply();
                            Toast.makeText(applicationContext, str("api_url_changed"), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }

        public void setEditTextRef(EditText editText) {
            editTextRef = new WeakReference<>(editText);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SponsorBlockSettings.update(getActivity());
    }
}
