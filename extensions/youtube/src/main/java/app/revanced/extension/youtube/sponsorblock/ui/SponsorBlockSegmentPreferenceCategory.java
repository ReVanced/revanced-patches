package app.revanced.extension.youtube.sponsorblock.ui;

import static app.revanced.extension.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ResettableEditTextPreference;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockViewController;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;

/**
 * Custom preference for managing segment categories and create segment settings independently.
 */
@SuppressWarnings({"unused", "deprecation"})
public class SponsorBlockSegmentPreferenceCategory extends PreferenceCategory {

    /**
     * ReVanced settings were recently imported and the UI needs to be updated.
     */
    public static boolean settingsImported;

    /**
     * If the preferences have been created and added to this group.
     */
    private boolean preferencesInitialized;

    private final List<SegmentCategoryListPreference> segmentCategories = new ArrayList<>();
    private SwitchPreference addNewSegment;
    private ResettableEditTextPreference newSegmentStep;
    private Preference guidelinePreferences;

    public SponsorBlockSegmentPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SponsorBlockSegmentPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SponsorBlockSegmentPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SponsorBlockSegmentPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    @SuppressLint("MissingSuperCall")
    protected View onCreateView(ViewGroup parent) {
        // Title is not shown.
        Logger.printDebug(() -> "SponsorBlockSegmentPreferenceCategory onCreateView called");
        return new View(getContext());
    }

    @Override
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        Logger.printDebug(() -> "SponsorBlockSegmentPreferenceCategory onAttachedToHierarchy called, preferencesInitialized: " + preferencesInitialized);

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
     * Initializes the segment category and create segment preferences.
     */
    private void initializePreferences() {
        try {
            Logger.printDebug(() -> "Initializing SponsorBlockSegmentPreferenceCategory");

            Context context = getContext();
            if (context == null) {
                Logger.printDebug(() -> "Context is null");
                return;
            }

            // Segment Categories Header
            Preference segmentHeader = new Preference(context);
            segmentHeader.setTitle(str("revanced_sb_diff_segments_title"));
            segmentHeader.setSelectable(false);
            addPreference(segmentHeader);

            // Segment Categories
            List<SegmentCategory> categories = Arrays.asList(SegmentCategory.categoriesWithoutUnsubmitted());
            if (categories == null) {
                Logger.printDebug(() -> "SegmentCategory.categoriesWithoutUnsubmitted returned null");
                return;
            }

            for (SegmentCategory category : categories) {
                SegmentCategoryListPreference categoryPreference = new SegmentCategoryListPreference(context, category);
                segmentCategories.add(categoryPreference);
                addPreference(categoryPreference);
            }

            // Create Segment Category Header
            Preference createSegmentHeader = new Preference(context);
            createSegmentHeader.setTitle(str("revanced_sb_create_segment_category_title"));
            createSegmentHeader.setSelectable(false);
            addPreference(createSegmentHeader);

            // Create Segment Preferences
            addNewSegment = new SwitchPreference(context);
            addNewSegment.setTitle(str("revanced_sb_enable_create_segment_title"));
            addNewSegment.setSummaryOn(str("revanced_sb_enable_create_segment_summary_on"));
            addNewSegment.setSummaryOff(str("revanced_sb_enable_create_segment_summary_off"));
            addNewSegment.setOnPreferenceChangeListener((preference, newValue) -> {
                Boolean value = (Boolean) newValue;
                if (value && !Settings.SB_SEEN_GUIDELINES.get()) {
                    new AlertDialog.Builder(preference.getContext())
                            .setTitle(str("revanced_sb_guidelines_popup_title"))
                            .setMessage(str("revanced_sb_guidelines_popup_content"))
                            .setNegativeButton(str("revanced_sb_guidelines_popup_already_read"), null)
                            .setPositiveButton(str("revanced_sb_guidelines_popup_open"), (dialogInterface, i) -> openGuidelines())
                            .setOnDismissListener(dialog -> Settings.SB_SEEN_GUIDELINES.save(true))
                            .setCancelable(false)
                            .show();
                }
                Settings.SB_CREATE_NEW_SEGMENT.save(value);
                updateUI();
                return true;
            });
            addPreference(addNewSegment);

            newSegmentStep = new ResettableEditTextPreference(context);
            newSegmentStep.setSetting(Settings.SB_CREATE_NEW_SEGMENT_STEP);
            newSegmentStep.setTitle(str("revanced_sb_general_adjusting_title"));
            newSegmentStep.setSummary(str("revanced_sb_general_adjusting_summary"));
            newSegmentStep.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            newSegmentStep.setOnPreferenceChangeListener((preference, newValue) -> {
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
            addPreference(newSegmentStep);

            guidelinePreferences = new Preference(context);
            guidelinePreferences.setTitle(str("revanced_sb_guidelines_preference_title"));
            guidelinePreferences.setSummary(str("revanced_sb_guidelines_preference_summary"));
            guidelinePreferences.setOnPreferenceClickListener(preference -> {
                openGuidelines();
                return true;
            });
            addPreference(guidelinePreferences);

            Utils.setPreferenceTitlesToMultiLineIfNeeded(this);

            updateUI();
        } catch (Exception ex) {
            Logger.printException(() -> "SponsorBlockSegmentPreferenceCategory initialization failure: " + ex.getMessage(), ex);
        }
    }

    /**
     * Updates the UI for all segment category and create segment preferences.
     */
    private void updateUI() {
        try {
            Logger.printDebug(() -> "Updating SponsorBlockSegmentPreferenceCategory UI");

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

            newSegmentStep.setText(Settings.SB_CREATE_NEW_SEGMENT_STEP.get().toString());
            newSegmentStep.setEnabled(enabled);

            for (SegmentCategoryListPreference category : segmentCategories) {
                category.setEnabled(enabled);
                category.updateUI();
            }

            guidelinePreferences.setEnabled(enabled);

        } catch (Exception ex) {
            Logger.printException(() -> "SponsorBlockSegmentPreferenceCategory updateUI failure: " + ex.getMessage(), ex);
        }
    }

    /**
     * Opens the SponsorBlock guidelines webpage.
     */
    private void openGuidelines() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wiki.sponsor.ajay.app/w/Guidelines"));
        getContext().startActivity(intent);
    }
}