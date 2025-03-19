package app.revanced.extension.youtube.settings.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.*;
import android.text.Html;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.preference.ResettableEditTextPreference;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SegmentPlaybackController;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockUtils;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategoryListPreference;
import app.revanced.extension.youtube.sponsorblock.objects.UserStats;
import app.revanced.extension.youtube.sponsorblock.requests.SBRequester;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockViewController;

import static android.text.Html.fromHtml;
import static app.revanced.extension.shared.StringRef.str;

@SuppressWarnings("deprecation")
public class SponsorBlockPreferenceFragment extends PreferenceFragment {

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

    private PreferenceCategory statsCategory;
    private PreferenceCategory segmentCategory;

    private void updateUI() {
        try {
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
            statsCategory.setEnabled(enabled);
        } catch (Exception ex) {
            Logger.printException(() -> "update settings UI failure", ex);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Activity context = getActivity();
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(Setting.preferences.name);
            PreferenceScreen preferenceScreen = manager.createPreferenceScreen(context);
            setPreferenceScreen(preferenceScreen);

            SponsorBlockSettings.initialize();

            sbEnabled = new SwitchPreference(context);
            sbEnabled.setTitle(str("revanced_sb_enable_sb"));
            sbEnabled.setSummary(str("revanced_sb_enable_sb_sum"));
            preferenceScreen.addPreference(sbEnabled);
            sbEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
                Settings.SB_ENABLED.save((Boolean) newValue);
                updateUI();
                return true;
            });

            addAppearanceCategory(context, preferenceScreen);

            segmentCategory = new PreferenceCategory(context);
            segmentCategory.setTitle(str("revanced_sb_diff_segments"));
            preferenceScreen.addPreference(segmentCategory);
            updateSegmentCategories();

            addCreateSegmentCategory(context, preferenceScreen);

            addGeneralCategory(context, preferenceScreen);

            statsCategory = new PreferenceCategory(context);
            statsCategory.setTitle(str("revanced_sb_stats"));
            preferenceScreen.addPreference(statsCategory);
            fetchAndDisplayStats();

            addAboutCategory(context, preferenceScreen);

            Utils.setPreferenceTitlesToMultiLineIfNeeded(preferenceScreen);

            updateUI();
        } catch (Exception ex) {
            Logger.printException(() -> "onCreate failure", ex);
        }
    }

    private void addAppearanceCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("revanced_sb_appearance_category"));

        votingEnabled = new SwitchPreference(context);
        votingEnabled.setTitle(str("revanced_sb_enable_voting"));
        votingEnabled.setSummaryOn(str("revanced_sb_enable_voting_sum_on"));
        votingEnabled.setSummaryOff(str("revanced_sb_enable_voting_sum_off"));
        category.addPreference(votingEnabled);
        votingEnabled.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_VOTING_BUTTON.save((Boolean) newValue);
            updateUI();
            return true;
        });

        autoHideSkipSegmentButton = new SwitchPreference(context);
        autoHideSkipSegmentButton.setTitle(str("revanced_sb_enable_auto_hide_skip_segment_button"));
        autoHideSkipSegmentButton.setSummaryOn(str("revanced_sb_enable_auto_hide_skip_segment_button_sum_on"));
        autoHideSkipSegmentButton.setSummaryOff(str("revanced_sb_enable_auto_hide_skip_segment_button_sum_off"));
        category.addPreference(autoHideSkipSegmentButton);
        autoHideSkipSegmentButton.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_AUTO_HIDE_SKIP_BUTTON.save((Boolean) newValue);
            updateUI();
            return true;
        });

        compactSkipButton = new SwitchPreference(context);
        compactSkipButton.setTitle(str("revanced_sb_enable_compact_skip_button"));
        compactSkipButton.setSummaryOn(str("revanced_sb_enable_compact_skip_button_sum_on"));
        compactSkipButton.setSummaryOff(str("revanced_sb_enable_compact_skip_button_sum_off"));
        category.addPreference(compactSkipButton);
        compactSkipButton.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_COMPACT_SKIP_BUTTON.save((Boolean) newValue);
            updateUI();
            return true;
        });

        squareLayout = new SwitchPreference(context);
        squareLayout.setTitle(str("revanced_sb_square_layout"));
        squareLayout.setSummaryOn(str("revanced_sb_square_layout_sum_on"));
        squareLayout.setSummaryOff(str("revanced_sb_square_layout_sum_off"));
        category.addPreference(squareLayout);
        squareLayout.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_SQUARE_LAYOUT.save((Boolean) newValue);
            updateUI();
            return true;
        });

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
        category.addPreference(showSkipToast);

        showTimeWithoutSegments = new SwitchPreference(context);
        showTimeWithoutSegments.setTitle(str("revanced_sb_general_time_without"));
        showTimeWithoutSegments.setSummaryOn(str("revanced_sb_general_time_without_sum_on"));
        showTimeWithoutSegments.setSummaryOff(str("revanced_sb_general_time_without_sum_off"));
        showTimeWithoutSegments.setOnPreferenceChangeListener((preference1, newValue) -> {
            Settings.SB_VIDEO_LENGTH_WITHOUT_SEGMENTS.save((Boolean) newValue);
            updateUI();
            return true;
        });
        category.addPreference(showTimeWithoutSegments);
    }

    private void addCreateSegmentCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
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

    private void addGeneralCategory(final Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
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
            fetchAndDisplayStats();
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
            fetchAndDisplayStats();
            updateUI();
            return true;
        });
        category.addPreference(importExport);
    }

    private void updateSegmentCategories() {
        try {
            segmentCategory.removeAll();

            Activity activity = getActivity();
            for (SegmentCategory category : SegmentCategory.categoriesWithoutUnsubmitted()) {
                segmentCategory.addPreference(new SegmentCategoryListPreference(activity, category));
            }
        } catch (Exception ex) {
            Logger.printException(() -> "updateSegmentCategories failure", ex);
        }
    }

    private void addAboutCategory(Context context, PreferenceScreen screen) {
        PreferenceCategory category = new PreferenceCategory(context);
        screen.addPreference(category);
        category.setTitle(str("revanced_sb_about"));

        {
            Preference preference = new Preference(context);
            category.addPreference(preference);
            preference.setTitle(str("revanced_sb_about_api"));
            preference.setSummary(str("revanced_sb_about_api_sum"));
            preference.setOnPreferenceClickListener(preference1 -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://sponsor.ajay.app"));
                preference1.getContext().startActivity(i);
                return false;
            });
        }
    }

    private void openGuidelines() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wiki.sponsor.ajay.app/w/Guidelines"));
        getActivity().startActivity(intent);
    }

    private void fetchAndDisplayStats() {
        try {
            statsCategory.removeAll();
            if (!SponsorBlockSettings.userHasSBPrivateId()) {
                // User has never voted or created any segments.  No stats to show.
                addLocalUserStats();
                return;
            }

            Preference loadingPlaceholderPreference = new Preference(this.getActivity());
            loadingPlaceholderPreference.setEnabled(false);
            statsCategory.addPreference(loadingPlaceholderPreference);
            if (Settings.SB_ENABLED.get()) {
                loadingPlaceholderPreference.setTitle(str("revanced_sb_stats_loading"));
                Utils.runOnBackgroundThread(() -> {
                    UserStats stats = SBRequester.retrieveUserStats();
                    Utils.runOnMainThread(() -> { // get back on main thread to modify UI elements
                        addUserStats(loadingPlaceholderPreference, stats);
                        addLocalUserStats();
                    });
                });
            } else {
                loadingPlaceholderPreference.setTitle(str("revanced_sb_stats_sb_disabled"));
            }
        } catch (Exception ex) {
            Logger.printException(() -> "fetchAndDisplayStats failure", ex);
        }
    }

    private void addUserStats(@NonNull Preference loadingPlaceholder, @Nullable UserStats stats) {
        Utils.verifyOnMainThread();
        try {
            if (stats == null) {
                loadingPlaceholder.setTitle(str("revanced_sb_stats_connection_failure"));
                return;
            }
            statsCategory.removeAll();
            Context context = statsCategory.getContext();

            if (stats.totalSegmentCountIncludingIgnored > 0) {
                // If user has not created any segments, there's no reason to set a username.
                EditTextPreference preference = new ResettableEditTextPreference(context);
                statsCategory.addPreference(preference);
                String userName = stats.userName;
                preference.setTitle(fromHtml(str("revanced_sb_stats_username", userName)));
                preference.setSummary(str("revanced_sb_stats_username_change"));
                preference.setText(userName);
                preference.setOnPreferenceChangeListener((preference1, value) -> {
                    Utils.runOnBackgroundThread(() -> {
                        String newUserName = (String) value;
                        String errorMessage = SBRequester.setUsername(newUserName);
                        Utils.runOnMainThread(() -> {
                            if (errorMessage == null) {
                                preference.setTitle(fromHtml(str("revanced_sb_stats_username", newUserName)));
                                preference.setText(newUserName);
                                Utils.showToastLong(str("revanced_sb_stats_username_changed"));
                            } else {
                                preference.setText(userName); // revert to previous
                                SponsorBlockUtils.showErrorDialog(errorMessage);
                            }
                        });
                    });
                    return true;
                });
            }

            {
                // number of segment submissions (does not include ignored segments)
                Preference preference = new Preference(context);
                statsCategory.addPreference(preference);
                String formatted = SponsorBlockUtils.getNumberOfSkipsString(stats.segmentCount);
                preference.setTitle(fromHtml(str("revanced_sb_stats_submissions", formatted)));
                preference.setSummary(str("revanced_sb_stats_submissions_sum"));
                if (stats.totalSegmentCountIncludingIgnored == 0) {
                    preference.setSelectable(false);
                } else {
                    preference.setOnPreferenceClickListener(preference1 -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("https://sb.ltn.fi/userid/" + stats.publicUserId));
                        preference1.getContext().startActivity(i);
                        return true;
                    });
                }
            }

            {
                // "user reputation".  Usually not useful, since it appears most users have zero reputation.
                // But if there is a reputation, then show it here
                Preference preference = new Preference(context);
                preference.setTitle(fromHtml(str("revanced_sb_stats_reputation", stats.reputation)));
                preference.setSelectable(false);
                if (stats.reputation != 0) {
                    statsCategory.addPreference(preference);
                }
            }

            {
                // time saved for other users
                Preference preference = new Preference(context);
                statsCategory.addPreference(preference);

                String stats_saved;
                String stats_saved_sum;
                if (stats.totalSegmentCountIncludingIgnored == 0) {
                    stats_saved = str("revanced_sb_stats_saved_zero");
                    stats_saved_sum = str("revanced_sb_stats_saved_sum_zero");
                } else {
                    stats_saved = str("revanced_sb_stats_saved",
                            SponsorBlockUtils.getNumberOfSkipsString(stats.viewCount));
                    stats_saved_sum = str("revanced_sb_stats_saved_sum", SponsorBlockUtils.getTimeSavedString((long) (60 * stats.minutesSaved)));
                }
                preference.setTitle(fromHtml(stats_saved));
                preference.setSummary(fromHtml(stats_saved_sum));
                preference.setOnPreferenceClickListener(preference1 -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://sponsor.ajay.app/stats/"));
                    preference1.getContext().startActivity(i);
                    return false;
                });
            }
        } catch (Exception ex) {
            Logger.printException(() -> "addUserStats failure", ex);
        }
    }

    private void addLocalUserStats() {
        // time the user saved by using SB
        Preference preference = new Preference(statsCategory.getContext());
        statsCategory.addPreference(preference);

        Runnable updateStatsSelfSaved = () -> {
            String formatted = SponsorBlockUtils.getNumberOfSkipsString(Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.get());
            preference.setTitle(fromHtml(str("revanced_sb_stats_self_saved", formatted)));
            String formattedSaved = SponsorBlockUtils.getTimeSavedString(Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.get() / 1000);
            preference.setSummary(fromHtml(str("revanced_sb_stats_self_saved_sum", formattedSaved)));
        };
        updateStatsSelfSaved.run();
        preference.setOnPreferenceClickListener(preference1 -> {
            new AlertDialog.Builder(preference1.getContext())
                    .setTitle(str("revanced_sb_stats_self_saved_reset_title"))
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.resetToDefault();
                        Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.resetToDefault();
                        updateStatsSelfSaved.run();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
        });
    }

}
