package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.*;
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
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;

/**
 * Lots of old code that could be converted to a half dozen custom preferences,
 * but instead it's wrapped in this group container and all logic is handled here.
 */
@SuppressWarnings({"unused", "deprecation"})
public class SponsorBlockLegacySettingsGroup extends PreferenceGroup {

    /**
     * Settings were recently imported, and the UI needs to be updated.
     */
    public static boolean settingsImported;

    private SwitchPreference addNewSegment;
    private SwitchPreference trackSkips;
    private SwitchPreference toastOnConnectionError;

    private ResettableEditTextPreference newSegmentStep;
    private ResettableEditTextPreference minSegmentDuration;
    private EditTextPreference privateUserId;
    private EditTextPreference importExport;
    private Preference apiUrl;

    private PreferenceCategory segmentCategory;

    public SponsorBlockLegacySettingsGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SponsorBlockLegacySettingsGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SponsorBlockLegacySettingsGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    @SuppressLint("MissingSuperCall")
    protected View onCreateView(ViewGroup parent) {
        // Title is not shown.
        return new View(getContext());
    }

    private void updateUI() {
        try {
            Logger.printDebug(() -> "updateUI");

            final boolean enabled = Settings.SB_ENABLED.get();
            if (!enabled) {
                SponsorBlockViewController.hideAll();
                SegmentPlaybackController.setCurrentVideoId(null);
            } else if (!Settings.SB_CREATE_NEW_SEGMENT.get()) {
                SponsorBlockViewController.hideNewSegmentLayout();
            }
            // Voting and add new segment buttons automatically show/hide themselves.

            SponsorBlockViewController.updateLayout();

            addNewSegment.setChecked(Settings.SB_CREATE_NEW_SEGMENT.get());
            addNewSegment.setEnabled(enabled);

            toastOnConnectionError.setChecked(Settings.SB_TOAST_ON_CONNECTION_ERROR.get());
            toastOnConnectionError.setEnabled(enabled);

            trackSkips.setChecked(Settings.SB_TRACK_SKIP_COUNT.get());
            trackSkips.setEnabled(enabled);

            newSegmentStep.setText((Settings.SB_CREATE_NEW_SEGMENT_STEP.get()).toString());
            newSegmentStep.setEnabled(enabled);

            minSegmentDuration.setText((Settings.SB_SEGMENT_MIN_DURATION.get()).toString());
            minSegmentDuration.setEnabled(enabled);

            privateUserId.setText(Settings.SB_PRIVATE_USER_ID.get());
            privateUserId.setEnabled(enabled);

            // If the user has a private user id, then include a subtext that mentions not to share it.
            String importExportSummary = SponsorBlockSettings.userHasSBPrivateId()
                    ? str("revanced_sb_settings_ie_sum_warning")
                    : str("revanced_sb_settings_ie_sum");
            importExport.setSummary(importExportSummary);

            apiUrl.setEnabled(enabled);
            importExport.setEnabled(enabled);
            segmentCategory.setEnabled(enabled);
        } catch (Exception ex) {
            Logger.printException(() -> "update settings UI failure", ex);
        }
    }

    protected void onAttachedToActivity() {
        try {
            super.onAttachedToActivity();

            if (addNewSegment != null) {
                if (settingsImported) {
                    settingsImported = false;
                    updateUI();
                }
                return;
            }

            Logger.printDebug(() -> "Creating settings preferences");
            Context context = getContext();
            SponsorBlockSettings.initialize();

            segmentCategory = new PreferenceCategory(context);
            segmentCategory.setTitle(str("revanced_sb_diff_segments"));
            addPreference(segmentCategory);
            updateSegmentCategories();

            addCreateSegmentCategory(context);

            addGeneralCategory(context);

            Utils.setPreferenceTitlesToMultiLineIfNeeded(this);

            updateUI();
        } catch (Exception ex) {
            Logger.printException(() -> "onAttachedToActivity failure", ex);
        }
    }

    private void addCreateSegmentCategory(Context context) {
        PreferenceCategory category = new PreferenceCategory(context);
        addPreference(category);
        category.setTitle(str("revanced_sb_create_segment_category"));

        addNewSegment = new SwitchPreference(context);
        addNewSegment.setTitle(str("revanced_sb_enable_create_segment"));
        addNewSegment.setSummaryOn(str("revanced_sb_enable_create_segment_sum_on"));
        addNewSegment.setSummaryOff(str("revanced_sb_enable_create_segment_sum_off"));
        category.addPreference(addNewSegment);
        addNewSegment.setOnPreferenceChangeListener((preference1, o) -> {
            Boolean newValue = (Boolean) o;
            if (newValue && !Settings.SB_SEEN_GUIDELINES.get()) {
                new AlertDialog.Builder(preference1.getContext())
                        .setTitle(str("revanced_sb_guidelines_popup_title"))
                        .setMessage(str("revanced_sb_guidelines_popup_content"))
                        .setNegativeButton(str("revanced_sb_guidelines_popup_already_read"), null)
                        .setPositiveButton(str("revanced_sb_guidelines_popup_open"), (dialogInterface, i) -> openGuidelines())
                        .setOnDismissListener(dialog -> Settings.SB_SEEN_GUIDELINES.save(true))
                        .setCancelable(false)
                        .show();
            }
            Settings.SB_CREATE_NEW_SEGMENT.save(newValue);
            updateUI();
            return true;
        });

        newSegmentStep = new ResettableEditTextPreference(context);
        newSegmentStep.setSetting(Settings.SB_CREATE_NEW_SEGMENT_STEP);
        newSegmentStep.setTitle(str("revanced_sb_general_adjusting"));
        newSegmentStep.setSummary(str("revanced_sb_general_adjusting_sum"));
        newSegmentStep.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        newSegmentStep.setOnPreferenceChangeListener((preference1, newValue) -> {
            try {
                final int newAdjustmentValue = Integer.parseInt(newValue.toString());
                if (newAdjustmentValue != 0) {
                    Settings.SB_CREATE_NEW_SEGMENT_STEP.save(newAdjustmentValue);
                    return true;
                }
            } catch (NumberFormatException ex) {
                Logger.printInfo(() -> "Invalid new segment step", ex);
            }

            Utils.showToastLong(str("revanced_sb_general_adjusting_invalid"));
            updateUI();
            return false;
        });
        category.addPreference(newSegmentStep);

        Preference guidelinePreferences = new Preference(context);
        guidelinePreferences.setTitle(str("revanced_sb_guidelines_preference_title"));
        guidelinePreferences.setSummary(str("revanced_sb_guidelines_preference_sum"));
        guidelinePreferences.setOnPreferenceClickListener(preference1 -> {
            openGuidelines();
            return true;
        });
        category.addPreference(guidelinePreferences);
    }

    private void addGeneralCategory(final Context context) {
        PreferenceCategory category = new PreferenceCategory(context);
        addPreference(category);
        category.setTitle(str("revanced_sb_general"));

        toastOnConnectionError = new SwitchPreference(context);
        toastOnConnectionError.setTitle(str("revanced_sb_toast_on_connection_error_title"));
        toastOnConnectionError.setSummaryOn(str("revanced_sb_toast_on_connection_error_summary_on"));
        toastOnConnectionError.setSummaryOff(str("revanced_sb_toast_on_connection_error_summary_off"));
        toastOnConnectionError.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_TOAST_ON_CONNECTION_ERROR.save((Boolean) newValue);
            updateUI();
            return true;
        });
        category.addPreference(toastOnConnectionError);

        trackSkips = new SwitchPreference(context);
        trackSkips.setTitle(str("revanced_sb_general_skipcount"));
        trackSkips.setSummaryOn(str("revanced_sb_general_skipcount_sum_on"));
        trackSkips.setSummaryOff(str("revanced_sb_general_skipcount_sum_off"));
        trackSkips.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_TRACK_SKIP_COUNT.save((Boolean) newValue);
            updateUI();
            return true;
        });
        category.addPreference(trackSkips);

        minSegmentDuration = new ResettableEditTextPreference(context);
        minSegmentDuration.setSetting(Settings.SB_SEGMENT_MIN_DURATION);
        minSegmentDuration.setTitle(str("revanced_sb_general_min_duration"));
        minSegmentDuration.setSummary(str("revanced_sb_general_min_duration_sum"));
        minSegmentDuration.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        minSegmentDuration.setOnPreferenceChangeListener((preference1, newValue) -> {
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
        category.addPreference(minSegmentDuration);

        privateUserId = new EditTextPreference(context) {
            protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                Utils.setEditTextDialogTheme(builder);

                builder.setNeutralButton(str("revanced_sb_settings_copy"), (dialog, which) -> {
                    Utils.setClipboard(getEditText().getText().toString());
                });
            }
        };
        privateUserId.setTitle(str("revanced_sb_general_uuid"));
        privateUserId.setSummary(str("revanced_sb_general_uuid_sum"));
        privateUserId.setOnPreferenceChangeListener((preference1, newValue) -> {
            String newUUID = newValue.toString();
            if (!SponsorBlockSettings.isValidSBUserId(newUUID)) {
                Utils.showToastLong(str("revanced_sb_general_uuid_invalid"));
                return false;
            }

            Settings.SB_PRIVATE_USER_ID.save(newUUID);
            updateUI();
            return true;
        });
        category.addPreference(privateUserId);

        apiUrl = new Preference(context);
        apiUrl.setTitle(str("revanced_sb_general_api_url"));
        apiUrl.setSummary(Html.fromHtml(str("revanced_sb_general_api_url_sum")));
        apiUrl.setOnPreferenceClickListener(preference1 -> {
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
        category.addPreference(apiUrl);

        importExport = new EditTextPreference(context) {
            protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                Utils.setEditTextDialogTheme(builder);

                builder.setNeutralButton(str("revanced_sb_settings_copy"), (dialog, which) -> {
                    Utils.setClipboard(getEditText().getText().toString());
                });
            }
        };
        importExport.setTitle(str("revanced_sb_settings_ie"));
        // Summary is set in updateUI()
        importExport.getEditText().setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        importExport.getEditText().setAutofillHints((String) null);
        importExport.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
        importExport.setOnPreferenceClickListener(preference1 -> {
            importExport.getEditText().setText(SponsorBlockSettings.exportDesktopSettings());
            return true;
        });
        importExport.setOnPreferenceChangeListener((preference1, newValue) -> {
            SponsorBlockSettings.importDesktopSettings((String) newValue);
            updateSegmentCategories();
            updateUI();
            return true;
        });
        category.addPreference(importExport);
    }

    private void updateSegmentCategories() {
        try {
            segmentCategory.removeAll();

            Context context = getContext();
            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                segmentCategory.addPreference(new SegmentCategoryListPreference(context, category));
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateSegmentCategories failure", ex);
        }
    }

    private void openGuidelines() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wiki.sponsor.ajay.app/w/Guidelines"));
        getContext().startActivity(intent);
    }
}
