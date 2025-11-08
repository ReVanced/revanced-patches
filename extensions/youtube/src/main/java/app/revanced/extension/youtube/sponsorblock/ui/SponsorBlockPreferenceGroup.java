package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.shared.StringRef.str;
import static app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController.SponsorBlockDuration;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.preference.CustomDialogListPreference;
import app.revanced.extension.shared.settings.preference.ResettableEditTextPreference;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.shared.ui.Dim;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryPreference;

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

    private EditTextPreference importExport;

    private final List<SegmentCategoryPreference> segmentCategories = new ArrayList<>();

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

            if (!Settings.SB_ENABLED.get()) {
                SponsorBlockViewController.hideAll();
                SegmentPlaybackController.setCurrentVideoId(null);
            } else if (!Settings.SB_CREATE_NEW_SEGMENT.get()) {
                SponsorBlockViewController.hideNewSegmentLayout();
            }

            SponsorBlockViewController.updateLayout();

            // Preferences are synced by AbstractPreferenceFragment since keys are set
            // and a Setting exist with the same key.

            // If the user has a private user id, then include a subtext that mentions not to share it.
            String importExportSummary = SponsorBlockSettings.userHasSBPrivateId()
                    ? str("revanced_sb_settings_ie_sum_warning")
                    : str("revanced_sb_settings_ie_sum");
            importExport.setSummary(importExportSummary);

            for (SegmentCategoryPreference category : segmentCategories) {
                category.updateUI();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateUI failure", ex);
        }
    }

    public void updateUIDelayed() {
        // Must use a delay, so AbstractPreferenceFragment can
        // update the availability of the settings.
        Utils.runOnMainThreadDelayed(this::updateUI, 50);
    }

    private void initializePreference(Preference preference, Setting<?> setting, String key) {
        initializePreference(preference, setting, key, true);
    }

    private void initializePreference(Preference preference, Setting<?> setting,
                                      String key, boolean setDetailedSummary) {
        preference.setKey(setting.key);
        preference.setTitle(str(key));
        preference.setEnabled(setting.isAvailable());
        boolean shouldSetSummary = true;

        if (preference instanceof SwitchPreference switchPref && setting instanceof BooleanSetting boolSetting) {
            switchPref.setChecked(boolSetting.get());
            if (setDetailedSummary) {
                switchPref.setSummaryOn(str(key + "_sum_on"));
                switchPref.setSummaryOff(str(key + "_sum_off"));
                shouldSetSummary = false;
            }
        } else if (preference instanceof ResettableEditTextPreference resetPref) {
            resetPref.setText(setting.get().toString());
        } else if (preference instanceof EditTextPreference editPref) {
            editPref.setText(setting.get().toString());
        } else if (preference instanceof ListPreference listPref) {
            listPref.setEntries(Utils.getResourceStringArray(key + "_entries"));
            listPref.setEntryValues(Utils.getResourceStringArray(key + "_entry_values"));
            listPref.setValue(setting.get().toString());

            if (preference instanceof CustomDialogListPreference dialogPref) {
                // Sets a static summary without overwriting it.
                dialogPref.setStaticSummary(str(key + "_sum"));
            }
        }

        if (shouldSetSummary) {
            preference.setSummary(str(key + "_sum"));
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

            SwitchPreference sbEnabled = new SwitchPreference(context);
            initializePreference(sbEnabled, Settings.SB_ENABLED,
                    "revanced_sb_enable_sb", false);
            addPreference(sbEnabled);
            sbEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_ENABLED.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });

            PreferenceCategory appearanceCategory = new PreferenceCategory(context);
            appearanceCategory.setTitle(str("revanced_sb_appearance_category"));
            addPreference(appearanceCategory);

            SwitchPreference votingEnabled = new SwitchPreference(context);
            initializePreference(votingEnabled, Settings.SB_VOTING_BUTTON,
                    "revanced_sb_enable_voting");
            votingEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_VOTING_BUTTON.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(votingEnabled);

            SwitchPreference compactSkipButton = new SwitchPreference(context);
            initializePreference(compactSkipButton, Settings.SB_COMPACT_SKIP_BUTTON,
                    "revanced_sb_enable_compact_skip_button");
            compactSkipButton.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_COMPACT_SKIP_BUTTON.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(compactSkipButton);

            SwitchPreference autoHideSkipSegmentButton = new SwitchPreference(context);
            initializePreference(autoHideSkipSegmentButton, Settings.SB_AUTO_HIDE_SKIP_BUTTON,
                    "revanced_sb_enable_auto_hide_skip_segment_button");
            autoHideSkipSegmentButton.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_AUTO_HIDE_SKIP_BUTTON.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(autoHideSkipSegmentButton);

            CustomDialogListPreference autoHideSkipSegmentButtonDuration = new CustomDialogListPreference(context);
            initializePreference(autoHideSkipSegmentButtonDuration, Settings.SB_AUTO_HIDE_SKIP_BUTTON_DURATION,
                    "revanced_sb_auto_hide_skip_button_duration");
            autoHideSkipSegmentButtonDuration.setOnPreferenceChangeListener((preference1, newValue) -> {
                SponsorBlockDuration newDuration = SponsorBlockDuration.valueOf((String) newValue);
                Settings.SB_AUTO_HIDE_SKIP_BUTTON_DURATION.save(newDuration);
                ((CustomDialogListPreference) preference1).setValue(newDuration.name());
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(autoHideSkipSegmentButtonDuration);

            SwitchPreference showSkipToast = new SwitchPreference(context);
            initializePreference(showSkipToast, Settings.SB_TOAST_ON_SKIP,
                    "revanced_sb_general_skiptoast");
            showSkipToast.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_TOAST_ON_SKIP.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(showSkipToast);

            CustomDialogListPreference showSkipToastDuration = new CustomDialogListPreference(context);
            initializePreference(showSkipToastDuration, Settings.SB_TOAST_ON_SKIP_DURATION,
                    "revanced_sb_toast_on_skip_duration");
            // Sets a static summary without overwriting it.
            showSkipToastDuration.setStaticSummary(str("revanced_sb_toast_on_skip_duration_sum"));
            showSkipToastDuration.setOnPreferenceChangeListener((preference1, newValue) -> {
                SponsorBlockDuration newDuration = SponsorBlockDuration.valueOf((String) newValue);
                Settings.SB_TOAST_ON_SKIP_DURATION.save(newDuration);
                ((CustomDialogListPreference) preference1).setValue(newDuration.name());
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(showSkipToastDuration);

            SwitchPreference showTimeWithoutSegments = new SwitchPreference(context);
            initializePreference(showTimeWithoutSegments, Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS,
                    "revanced_sb_general_time_without");
            showTimeWithoutSegments.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(showTimeWithoutSegments);

            SwitchPreference squareLayout = new SwitchPreference(context);
            initializePreference(squareLayout, Settings.SB_SQUARE_LAYOUT,
                    "revanced_sb_square_layout");
            squareLayout.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_SQUARE_LAYOUT.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            appearanceCategory.addPreference(squareLayout);

            PreferenceCategory segmentCategory = new PreferenceCategory(context);
            segmentCategory.setTitle(str("revanced_sb_diff_segments"));
            addPreference(segmentCategory);

            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                SegmentCategoryPreference categoryPreference = new SegmentCategoryPreference(context, category);
                segmentCategories.add(categoryPreference);
                segmentCategory.addPreference(categoryPreference);
            }

            PreferenceCategory createSegmentCategory = new PreferenceCategory(context);
            createSegmentCategory.setTitle(str("revanced_sb_create_segment_category"));
            addPreference(createSegmentCategory);

            SwitchPreference addNewSegment = new SwitchPreference(context);
            initializePreference(addNewSegment, Settings.SB_CREATE_NEW_SEGMENT,
                    "revanced_sb_enable_create_segment");
            addNewSegment.setOnPreferenceChangeListener((preference1, o) -> {
                Boolean newValue = (Boolean) o;
                if (newValue && !Settings.SB_SEEN_GUIDELINES.get()) {
                    Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                            preference1.getContext(),
                            str("revanced_sb_guidelines_popup_title"),      // Title.
                            str("revanced_sb_guidelines_popup_content"),    // Message.
                            null,                                               // No EditText.
                            str("revanced_sb_guidelines_popup_open"),       // OK button text.
                            this::openGuidelines,                               // OK button action.
                            null,                                               // Cancel button action.
                            str("revanced_sb_guidelines_popup_already_read"), // Neutral button text.
                            () -> {},                                           // Neutral button action (dismiss only).
                            true                                                // Dismiss dialog when onNeutralClick.
                    );

                    // Set dialog as non-cancelable.
                    dialogPair.first.setCancelable(false);

                    dialogPair.first.setOnDismissListener(dialog -> Settings.SB_SEEN_GUIDELINES.save(true));

                    // Show the dialog.
                    dialogPair.first.show();
                }
                Settings.SB_CREATE_NEW_SEGMENT.save(newValue);
                updateUIDelayed();
                return true;
            });
            createSegmentCategory.addPreference(addNewSegment);

            ResettableEditTextPreference newSegmentStep = new ResettableEditTextPreference(context);
            initializePreference(newSegmentStep, Settings.SB_CREATE_NEW_SEGMENT_STEP,
                    "revanced_sb_general_adjusting");
            newSegmentStep.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            newSegmentStep.setOnPreferenceChangeListener((preference1, newValue) -> {
                try {
                    final int newAdjustmentValue = Integer.parseInt(newValue.toString());
                    if (newAdjustmentValue != 0) {
                        Settings.SB_CREATE_NEW_SEGMENT_STEP.save(newAdjustmentValue);
                        updateUIDelayed();
                        return true;
                    }
                } catch (NumberFormatException ex) {
                    Logger.printInfo(() -> "Invalid new segment step", ex);
                }

                Utils.showToastLong(str("revanced_sb_general_adjusting_invalid"));
                updateUIDelayed();
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

            SwitchPreference toastOnConnectionError = new SwitchPreference(context);
            initializePreference(toastOnConnectionError, Settings.SB_TOAST_ON_CONNECTION_ERROR,
                    "revanced_sb_toast_on_connection_error");
            toastOnConnectionError.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_TOAST_ON_CONNECTION_ERROR.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            generalCategory.addPreference(toastOnConnectionError);

            SwitchPreference trackSkips = new SwitchPreference(context);
            initializePreference(trackSkips, Settings.SB_TRACK_SKIP_COUNT,
                    "revanced_sb_general_skipcount");
            trackSkips.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_TRACK_SKIP_COUNT.save((Boolean) newValue);
                updateUIDelayed();
                return true;
            });
            generalCategory.addPreference(trackSkips);

            ResettableEditTextPreference minSegmentDuration = new ResettableEditTextPreference(context);
            initializePreference(minSegmentDuration, Settings.SB_SEGMENT_MIN_DURATION,
                    "revanced_sb_general_min_duration");
            minSegmentDuration.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            minSegmentDuration.setOnPreferenceChangeListener((preference1, newValue) -> {
                try {
                    Float minTimeDuration = Float.valueOf(newValue.toString());
                    Settings.SB_SEGMENT_MIN_DURATION.save(minTimeDuration);
                    updateUIDelayed();
                    return true;
                } catch (NumberFormatException ex) {
                    Logger.printInfo(() -> "Invalid minimum segment duration", ex);
                }

                Utils.showToastLong(str("revanced_sb_general_min_duration_invalid"));
                updateUIDelayed();
                return false;
            });
            generalCategory.addPreference(minSegmentDuration);

            EditTextPreference privateUserId = new EditTextPreference(context) {
                @Override
                protected void showDialog(Bundle state) {
                    try {
                        Context context = getContext();
                        EditText editText = getEditText();

                        // Set initial EditText value to the current persisted value or empty string.
                        String initialValue = getText() != null ? getText() : "";
                        editText.setText(initialValue);
                        editText.setSelection(initialValue.length()); // Move cursor to end.

                        // Create custom dialog.
                        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                                context,
                                getTitle() != null ? getTitle().toString() : "", // Title.
                                null,     // Message is replaced by EditText.
                                editText, // Pass the EditText.
                                null,     // OK button text.
                                () -> {
                                    // OK button action. Persist the EditText value when OK is clicked.
                                    String newValue = editText.getText().toString();
                                    if (callChangeListener(newValue)) {
                                        setText(newValue);
                                    }
                                },
                                () -> {}, // Cancel button action (dismiss only).
                                str("revanced_sb_settings_copy"), // Neutral button text (Copy).
                                () -> {
                                    // Neutral button action (Copy).
                                    try {
                                        Utils.setClipboard(getEditText().getText());
                                    } catch (Exception ex) {
                                        Logger.printException(() -> "Copy settings failure", ex);
                                    }
                                },
                                true // Dismiss dialog when onNeutralClick.
                        );

                        // Set dialog as cancelable.
                        dialogPair.first.setCancelable(true);

                        // Show the dialog.
                        dialogPair.first.show();
                    } catch (Exception ex) {
                        Logger.printException(() -> "showDialog failure", ex);
                    }
                }
            };
            initializePreference(privateUserId, Settings.SB_PRIVATE_USER_ID,
                    "revanced_sb_general_uuid");
            privateUserId.setOnPreferenceChangeListener((preference1, newValue) -> {
                String newUUID = newValue.toString();
                if (!SponsorBlockSettings.isValidSBUserId(newUUID)) {
                    Utils.showToastLong(str("revanced_sb_general_uuid_invalid"));
                    updateUIDelayed();
                    return false;
                }

                Settings.SB_PRIVATE_USER_ID.save(newUUID);
                updateUIDelayed();
                return true;
            });
            generalCategory.addPreference(privateUserId);

            Preference apiUrl = new Preference(context);
            initializePreference(apiUrl, Settings.SB_API_URL,
                    "revanced_sb_general_api_url");
            apiUrl.setOnPreferenceClickListener(preference1 -> {
                EditText editText = new EditText(context);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                editText.setText(Settings.SB_API_URL.get());

                // Create a custom dialog.
                Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                        context,
                        str("revanced_sb_general_api_url"), // Title.
                        null,     // No message, EditText replaces it.
                        editText, // Pass the EditText.
                        null,     // OK button text.
                        () -> {
                            // OK button action.
                            String serverAddress = editText.getText().toString();
                            if (!SponsorBlockSettings.isValidSBServerAddress(serverAddress)) {
                                Utils.showToastLong(str("revanced_sb_api_url_invalid"));
                            } else if (!serverAddress.equals(Settings.SB_API_URL.get())) {
                                Settings.SB_API_URL.save(serverAddress);
                                Utils.showToastLong(str("revanced_sb_api_url_changed"));
                            }
                        },
                        () -> {}, // Cancel button action (dismiss dialog).
                        str("revanced_settings_reset"), // Neutral (Reset) button text.
                        () -> {
                            // Neutral button action.
                            Settings.SB_API_URL.resetToDefault();
                            Utils.showToastLong(str("revanced_sb_api_url_reset"));
                        },
                        true // Dismiss dialog when onNeutralClick.
                );

                // Show the dialog.
                dialogPair.first.show();
                return true;
            });
            generalCategory.addPreference(apiUrl);

            importExport = new EditTextPreference(context) {
                @Override
                protected void showDialog(Bundle state) {
                    try {
                        Context context = getContext();
                        EditText editText = getEditText();

                        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        editText.setTextSize(14);

                        // Create a custom dialog.
                        Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                                context,
                                str("revanced_sb_settings_ie"), // Title.
                                null,     // No message, EditText replaces it.
                                editText, // Pass the EditText.
                                str("revanced_settings_import"), // OK button text.
                                () -> {
                                    // OK button action. Trigger OnPreferenceChangeListener.
                                    String newValue = editText.getText().toString();
                                    if (getOnPreferenceChangeListener() != null) {
                                        getOnPreferenceChangeListener().onPreferenceChange(this, newValue);
                                    }
                                },
                                () -> {}, // Cancel button action (dismiss only).
                                str("revanced_sb_settings_copy"), // Neutral button text (Copy).
                                () -> {
                                    // Neutral button action (Copy).
                                    try {
                                        Utils.setClipboard(editText.getText());
                                    } catch (Exception ex) {
                                        Logger.printException(() -> "Copy settings failure", ex);
                                    }
                                },
                                true // Dismiss dialog when onNeutralClick.
                        );

                        // Show the dialog.
                        dialogPair.first.show();
                    } catch (Exception ex) {
                        Logger.printException(() -> "showDialog failure", ex);
                    }
                }
            };
            importExport.setTitle(str("revanced_sb_settings_ie"));
            // Summary is set in updateUI().
            EditText editText = importExport.getEditText();
            editText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setAutofillHints((String) null);
            editText.setTextSize(14);

            // Set preference listeners.
            importExport.setOnPreferenceClickListener(preference1 -> {
                importExport.getEditText().setText(SponsorBlockSettings.exportDesktopSettings());
                return true;
            });
            importExport.setOnPreferenceChangeListener((preference1, newValue) -> {
                SponsorBlockSettings.importDesktopSettings((String) newValue);
                updateUIDelayed();
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
