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

import java.util.ArrayList;
import java.util.List;

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
public class SponsorBlockPreferenceGroup extends PreferenceGroup {

    /**
     * ReVanced settings were recently imported and the UI needs to be updated.
     */
    public static boolean settingsImported;

    /**
     * If the preferences have been created and added to this group.
     */
    private boolean preferencesInitialized;

    private SwitchPreference sbEnabled;
    private SwitchPreference addNewSegment;
    private SwitchPreference votingEnabled;
    private SwitchPreference autoHideSkipSegmentButton;
    private SwitchPreference compactSkipButton;
    private SwitchPreference squareLayout;
    private SwitchPreference showSkipToast;
    private SwitchPreference trackSkips;
    private SwitchPreference showTimeWithoutSegments;
    private SwitchPreference toastOnConnectionError;

    private ResettableEditTextPreference newSegmentStep;
    private ResettableEditTextPreference minSegmentDuration;
    private EditTextPreference privateUserId;
    private EditTextPreference importExport;
    private Preference apiUrl;

    private final List<SegmentCategoryListPreference> segmentCategories = new ArrayList<>();
    private PreferenceCategory segmentCategory;

    public SponsorBlockPreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SponsorBlockPreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SponsorBlockPreferenceGroup(Context context, AttributeSet attrs) {
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

            sbEnabled.setChecked(enabled);

            addNewSegment.setChecked(Settings.SB_CREATE_NEW_SEGMENT.get());
            addNewSegment.setEnabled(enabled);

            votingEnabled.setChecked(Settings.SB_VOTING_BUTTON.get());
            votingEnabled.setEnabled(enabled);

            autoHideSkipSegmentButton.setEnabled(enabled);
            autoHideSkipSegmentButton.setChecked(Settings.SB_AUTO_HIDE_SKIP_BUTTON.get());

            compactSkipButton.setChecked(Settings.SB_COMPACT_SKIP_BUTTON.get());
            compactSkipButton.setEnabled(enabled);

            squareLayout.setChecked(Settings.SB_SQUARE_LAYOUT.get());
            squareLayout.setEnabled(enabled);

            showSkipToast.setChecked(Settings.SB_TOAST_ON_SKIP.get());
            showSkipToast.setEnabled(enabled);

            toastOnConnectionError.setChecked(Settings.SB_TOAST_ON_CONNECTION_ERROR.get());
            toastOnConnectionError.setEnabled(enabled);

            trackSkips.setChecked(Settings.SB_TRACK_SKIP_COUNT.get());
            trackSkips.setEnabled(enabled);

            showTimeWithoutSegments.setChecked(Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.get());
            showTimeWithoutSegments.setEnabled(enabled);

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

            for (SegmentCategoryListPreference category : segmentCategories) {
                category.updateUI();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateUI failure", ex);
        }
    }

    protected void onAttachedToActivity() {
        try {
            super.onAttachedToActivity();

             if (preferencesInitialized) {
                if (settingsImported) {
                    settingsImported = false;
                    updateUI();
                }
                return;
            }

            preferencesInitialized = true;

            Logger.printDebug(() -> "Creating settings preferences");
            Context context = getContext();
            SponsorBlockSettings.initialize();

            sbEnabled = new SwitchPreference(context);
            sbEnabled.setTitle(str("revanced_sb_enable_sb"));
            sbEnabled.setSummary(str("revanced_sb_enable_sb_sum"));
            addPreference(sbEnabled);
            sbEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_ENABLED.save((Boolean) newValue);
                updateUI();
                return true;
            });

            PreferenceCategory appearanceCategory = new PreferenceCategory(context);
            appearanceCategory.setTitle(str("revanced_sb_appearance_category"));
            addPreference(appearanceCategory);

            votingEnabled = new SwitchPreference(context);
            votingEnabled.setTitle(str("revanced_sb_enable_voting"));
            votingEnabled.setSummaryOn(str("revanced_sb_enable_voting_sum_on"));
            votingEnabled.setSummaryOff(str("revanced_sb_enable_voting_sum_off"));
            votingEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_VOTING_BUTTON.save((Boolean) newValue);
                updateUI();
                return true;
            });
            appearanceCategory.addPreference(votingEnabled);

            autoHideSkipSegmentButton = new SwitchPreference(context);
            autoHideSkipSegmentButton.setTitle(str("revanced_sb_enable_auto_hide_skip_segment_button"));
            autoHideSkipSegmentButton.setSummaryOn(str("revanced_sb_enable_auto_hide_skip_segment_button_sum_on"));
            autoHideSkipSegmentButton.setSummaryOff(str("revanced_sb_enable_auto_hide_skip_segment_button_sum_off"));
            autoHideSkipSegmentButton.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_AUTO_HIDE_SKIP_BUTTON.save((Boolean) newValue);
                updateUI();
                return true;
            });
            appearanceCategory.addPreference(autoHideSkipSegmentButton);

            compactSkipButton = new SwitchPreference(context);
            compactSkipButton.setTitle(str("revanced_sb_enable_compact_skip_button"));
            compactSkipButton.setSummaryOn(str("revanced_sb_enable_compact_skip_button_sum_on"));
            compactSkipButton.setSummaryOff(str("revanced_sb_enable_compact_skip_button_sum_off"));
            compactSkipButton.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_COMPACT_SKIP_BUTTON.save((Boolean) newValue);
                updateUI();
                return true;
            });
            appearanceCategory.addPreference(compactSkipButton);

            squareLayout = new SwitchPreference(context);
            squareLayout.setTitle(str("revanced_sb_square_layout"));
            squareLayout.setSummaryOn(str("revanced_sb_square_layout_sum_on"));
            squareLayout.setSummaryOff(str("revanced_sb_square_layout_sum_off"));
            squareLayout.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_SQUARE_LAYOUT.save((Boolean) newValue);
                updateUI();
                return true;
            });
            appearanceCategory.addPreference(squareLayout);

            showSkipToast = new SwitchPreference(context);
            showSkipToast.setTitle(str("revanced_sb_general_skiptoast"));
            showSkipToast.setSummaryOn(str("revanced_sb_general_skiptoast_sum_on"));
            showSkipToast.setSummaryOff(str("revanced_sb_general_skiptoast_sum_off"));
            showSkipToast.setOnPreferenceClickListener(preference1 -> {
                Utils.showToastShort(str("revanced_sb_skipped_sponsor"));
                return false;
            });
            showSkipToast.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_TOAST_ON_SKIP.save((Boolean) newValue);
                updateUI();
                return true;
            });
            appearanceCategory.addPreference(showSkipToast);

            showTimeWithoutSegments = new SwitchPreference(context);
            showTimeWithoutSegments.setTitle(str("revanced_sb_general_time_without"));
            showTimeWithoutSegments.setSummaryOn(str("revanced_sb_general_time_without_sum_on"));
            showTimeWithoutSegments.setSummaryOff(str("revanced_sb_general_time_without_sum_off"));
            showTimeWithoutSegments.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.save((Boolean) newValue);
                updateUI();
                return true;
            });
            appearanceCategory.addPreference(showTimeWithoutSegments);

            segmentCategory = new PreferenceCategory(context);
            segmentCategory.setTitle(str("revanced_sb_diff_segments"));
            addPreference(segmentCategory);

            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                SegmentCategoryListPreference categoryPreference = new SegmentCategoryListPreference(context, category);
                segmentCategories.add(categoryPreference);
                segmentCategory.addPreference(categoryPreference);
            }

            PreferenceCategory createSegmentCategory = new PreferenceCategory(context);
            createSegmentCategory.setTitle(str("revanced_sb_create_segment_category"));
            addPreference(createSegmentCategory);

            addNewSegment = new SwitchPreference(context);
            addNewSegment.setTitle(str("revanced_sb_enable_create_segment"));
            addNewSegment.setSummaryOn(str("revanced_sb_enable_create_segment_sum_on"));
            addNewSegment.setSummaryOff(str("revanced_sb_enable_create_segment_sum_off"));
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
            createSegmentCategory.addPreference(addNewSegment);

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
            createSegmentCategory.addPreference(newSegmentStep);

            Preference guidelinePreferences = new Preference(context);
            guidelinePreferences.setTitle(str("revanced_sb_guidelines_preference_title"));
            guidelinePreferences.setSummary(str("revanced_sb_guidelines_preference_sum"));
            guidelinePreferences.setOnPreferenceClickListener(preference1 -> {
                openGuidelines();
                return true;
            });
            createSegmentCategory.addPreference(guidelinePreferences);

            PreferenceCategory generalCategory = new PreferenceCategory(context);
            generalCategory.setTitle(str("revanced_sb_general"));
            addPreference(generalCategory);

            toastOnConnectionError = new SwitchPreference(context);
            toastOnConnectionError.setTitle(str("revanced_sb_toast_on_connection_error_title"));
            toastOnConnectionError.setSummaryOn(str("revanced_sb_toast_on_connection_error_summary_on"));
            toastOnConnectionError.setSummaryOff(str("revanced_sb_toast_on_connection_error_summary_off"));
            toastOnConnectionError.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_TOAST_ON_CONNECTION_ERROR.save((Boolean) newValue);
                updateUI();
                return true;
            });
            generalCategory.addPreference(toastOnConnectionError);

            trackSkips = new SwitchPreference(context);
            trackSkips.setTitle(str("revanced_sb_general_skipcount"));
            trackSkips.setSummaryOn(str("revanced_sb_general_skipcount_sum_on"));
            trackSkips.setSummaryOff(str("revanced_sb_general_skipcount_sum_off"));
            trackSkips.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_TRACK_SKIP_COUNT.save((Boolean) newValue);
                updateUI();
                return true;
            });
            generalCategory.addPreference(trackSkips);

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
            generalCategory.addPreference(minSegmentDuration);

            privateUserId = new EditTextPreference(context) {
                protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                    Utils.setEditTextDialogTheme(builder);

                    builder.setNeutralButton(str("revanced_sb_settings_copy"), (dialog, which) -> {
                        try {
                            Utils.setClipboard(getEditText().getText());
                        } catch (Exception ex) {
                            Logger.printException(() -> "Copy settings failure", ex);
                        }
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
            generalCategory.addPreference(privateUserId);

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
                        .setNeutralButton(str("revanced_settings_reset"), urlChangeListener)
                        .setPositiveButton(android.R.string.ok, urlChangeListener)
                        .show();
                return true;
            });
            generalCategory.addPreference(apiUrl);

            importExport = new EditTextPreference(context) {
                protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
                    Utils.setEditTextDialogTheme(builder);

                    builder.setNeutralButton(str("revanced_sb_settings_copy"), (dialog, which) -> {
                        try {
                            Utils.setClipboard(getEditText().getText());
                        } catch (Exception ex) {
                            Logger.printException(() -> "Copy settings failure", ex);
                        }
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
                updateUI();
                return true;
            });
            generalCategory.addPreference(importExport);

            Utils.setPreferenceTitlesToMultiLineIfNeeded(this);

            updateUI();
        } catch (Exception ex) {
            Logger.printException(() -> "onAttachedToActivity failure", ex);
        }
    }

    private void openGuidelines() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wiki.sponsor.ajay.app/w/Guidelines"));
        getContext().startActivity(intent);
    }
}
