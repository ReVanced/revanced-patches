package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.text.Html;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ResettableEditTextPreference;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockViewController;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;

/**
 * Custom preference group for managing General SponsorBlock settings independently.
 */
@SuppressWarnings({"unused", "deprecation"})
public class SponsorBlockGeneralSettingsPreferenceCategory extends PreferenceCategory {

    /**
     * ReVanced settings were recently imported and the UI needs to be updated.
     */
    public static boolean settingsImported;

    /**
     * If the preferences have been created and added to this group.
     */
    private boolean preferencesInitialized;

    private SwitchPreference toastOnConnectionError;
    private SwitchPreference trackSkips;
    private ResettableEditTextPreference minSegmentDuration;
    private EditTextPreference privateUserId;
    private EditTextPreference importExport;
    private Preference apiUrl;

    public SponsorBlockGeneralSettingsPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SponsorBlockGeneralSettingsPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SponsorBlockGeneralSettingsPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SponsorBlockGeneralSettingsPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    @SuppressLint("MissingSuperCall")
    protected View onCreateView(ViewGroup parent) {
        // Title is not shown.
        Logger.printDebug(() -> "SponsorBlockGeneralSettingsPreferenceCategory onCreateView called");
        return new View(getContext());
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        Logger.printDebug(() -> "SponsorBlockGeneralSettingsPreferenceCategory onAttachedToHierarchy called, preferencesInitialized: " + preferencesInitialized);

        if (preferencesInitialized) {
            if (settingsImported) {
                Logger.printDebug(() -> "Settings imported, updating UI");
                settingsImported = false;
                updateUI();
            }
            return;
        }

        preferencesInitialized = true;
        initializePreferences();
    }

    /**
     * Initializes all General SponsorBlock settings preferences.
     */
    private void initializePreferences() {
        try {
            Logger.printDebug(() -> "Initializing SponsorBlockGeneralSettingsPreferenceCategory");
            Context context = getContext();
            SponsorBlockSettings.initialize();

            // General Settings Header
            Preference generalHeader = new Preference(context);
            generalHeader.setTitle(str("revanced_sb_general_title"));
            generalHeader.setSelectable(false);
            addPreference(generalHeader);

            toastOnConnectionError = new SwitchPreference(context);
            toastOnConnectionError.setTitle(str("revanced_sb_toast_on_connection_error_title"));
            toastOnConnectionError.setSummaryOn(str("revanced_sb_toast_on_connection_error_summary_on"));
            toastOnConnectionError.setSummaryOff(str("revanced_sb_toast_on_connection_error_summary_off"));
            toastOnConnectionError.setOnPreferenceChangeListener((preference, newValue) -> {
                Settings.SB_TOAST_ON_CONNECTION_ERROR.save((Boolean) newValue);
                updateUI();
                return true;
            });
            addPreference(toastOnConnectionError);

            trackSkips = new SwitchPreference(context);
            trackSkips.setTitle(str("revanced_sb_general_skip_count_title"));
            trackSkips.setSummaryOn(str("revanced_sb_general_skip_count_summary_on"));
            trackSkips.setSummaryOff(str("revanced_sb_general_skip_count_summary_off"));
            trackSkips.setOnPreferenceChangeListener((preference, newValue) -> {
                Settings.SB_TRACK_SKIP_COUNT.save((Boolean) newValue);
                updateUI();
                return true;
            });
            addPreference(trackSkips);

            minSegmentDuration = new ResettableEditTextPreference(context);
            minSegmentDuration.setSetting(Settings.SB_SEGMENT_MIN_DURATION);
            minSegmentDuration.setTitle(str("revanced_sb_general_min_duration_title"));
            minSegmentDuration.setSummary(str("revanced_sb_general_min_duration_summary"));
            minSegmentDuration.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            minSegmentDuration.setOnPreferenceChangeListener((preference, newValue) -> {
                try {
                    Float minTimeDuration = Float.valueOf(newValue.toString());
                    Settings.SB_SEGMENT_MIN_DURATION.save(minTimeDuration);
                    return true;
                } catch (NumberFormatException ex) {
                    Logger.printInfo(() -> "Invalid minimum segment duration", ex);
                }
                Utils.showToastLong(str("revanced_sb_general_min_duration_invalid"));
                updateUI();
                return false;
            });
            addPreference(minSegmentDuration);

            privateUserId = new EditTextPreference(context) {
                protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                    Utils.setEditTextDialogTheme(builder);
                    builder.setNeutralButton(str("revanced_sb_settings_copy"), (dialog, which) -> {
                        Utils.setClipboard(getEditText().getText().toString());
                    });
                }
            };
            privateUserId.setTitle(str("revanced_sb_general_uuid_title"));
            privateUserId.setSummary(str("revanced_sb_general_uuid_summary"));
            privateUserId.setOnPreferenceChangeListener((preference, newValue) -> {
                String newUUID = newValue.toString();
                if (!SponsorBlockSettings.isValidSBUserId(newUUID)) {
                    Utils.showToastLong(str("revanced_sb_general_uuid_invalid"));
                    return false;
                }
                Settings.SB_PRIVATE_USER_ID.save(newUUID);
                updateUI();
                return true;
            });
            addPreference(privateUserId);

            apiUrl = new Preference(context);
            apiUrl.setTitle(str("revanced_sb_general_api_url_title"));
            apiUrl.setSummary(Html.fromHtml(str("revanced_sb_general_api_url_summary")));
            apiUrl.setOnPreferenceClickListener(preference -> {
                EditText editText = new EditText(context);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                editText.setText(Settings.SB_API_URL.get());

                DialogInterface.OnClickListener urlChangeListener = (dialog, buttonPressed) -> {
                    if (buttonPressed == DialogInterface.BUTTON_NEUTRAL) {
                        Settings.SB_API_URL.resetToDefault();
                        Utils.showToastLong(str("revanced_sb_api_url_reset"));
                    } else if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                        String serverAddress = editText.getText().toString();
                        if (!SponsorBlockSettings.isValidSBServerAddress(serverAddress)) {
                            Utils.showToastLong(str("revanced_sb_api_url_invalid"));
                        } else if (!serverAddress.equals(Settings.SB_API_URL.get())) {
                            Settings.SB_API_URL.save(serverAddress);
                            Utils.showToastLong(str("revanced_sb_api_url_changed"));
                        }
                    }
                };
                new AlertDialog.Builder(context)
                        .setTitle(apiUrl.getTitle())
                        .setView(editText)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton(str("revanced_sb_reset"), urlChangeListener)
                        .setPositiveButton(android.R.string.ok, urlChangeListener)
                        .show();
                return true;
            });
            addPreference(apiUrl);

            importExport = new EditTextPreference(context) {
                protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                    Utils.setEditTextDialogTheme(builder);
                    builder.setNeutralButton(str("revanced_sb_settings_copy"), (dialog, which) -> {
                        Utils.setClipboard(getEditText().getText().toString());
                    });
                }
            };
            importExport.setTitle(str("revanced_sb_settings_ie_title"));
            importExport.getEditText().setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            importExport.getEditText().setAutofillHints((String) null);
            importExport.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
            importExport.setOnPreferenceClickListener(preference -> {
                importExport.getEditText().setText(SponsorBlockSettings.exportDesktopSettings());
                return true;
            });
            importExport.setOnPreferenceChangeListener((preference, newValue) -> {
                SponsorBlockSettings.importDesktopSettings((String) newValue);
                updateUI();
                return true;
            });
            addPreference(importExport);

            Utils.setPreferenceTitlesToMultiLineIfNeeded(this);

            updateUI();
        } catch (Exception ex) {
            Logger.printException(() -> "SponsorBlockGeneralSettingsPreferenceCategory initialization failure: " + ex.getMessage(), ex);
        }
    }

    /**
     * Updates the UI for all General preferences.
     */
    private void updateUI() {
        try {
            Logger.printDebug(() -> "Updating SponsorBlockGeneralSettingsPreferenceCategory UI");

            final boolean enabled = Settings.SB_ENABLED.get();
            if (!enabled) {
                SponsorBlockViewController.hideAll();
                SegmentPlaybackController.setCurrentVideoId(null);
            }
            SponsorBlockViewController.updateLayout();

            toastOnConnectionError.setChecked(Settings.SB_TOAST_ON_CONNECTION_ERROR.get());
            toastOnConnectionError.setEnabled(enabled);

            trackSkips.setChecked(Settings.SB_TRACK_SKIP_COUNT.get());
            trackSkips.setEnabled(enabled);

            minSegmentDuration.setText(Settings.SB_SEGMENT_MIN_DURATION.get().toString());
            minSegmentDuration.setEnabled(enabled);

            privateUserId.setText(Settings.SB_PRIVATE_USER_ID.get());
            privateUserId.setEnabled(enabled);

            String importExportSummary = SponsorBlockSettings.userHasSBPrivateId()
                    ? str("revanced_sb_settings_ie_summary_warning")
                    : str("revanced_sb_settings_ie_summary");
            importExport.setSummary(importExportSummary);

            apiUrl.setEnabled(enabled);
            importExport.setEnabled(enabled);

        } catch (Exception ex) {
            Logger.printException(() -> "SponsorBlockGeneralSettingsPreferenceCategory updateUI failure: " + ex.getMessage(), ex);
        }
    }
}