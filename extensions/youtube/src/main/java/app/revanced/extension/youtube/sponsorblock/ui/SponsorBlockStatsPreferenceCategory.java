package app.revanced.extension.youtube.sponsorblock.ui;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static android.text.Html.fromHtml;
import static app.revanced.extension.shared.StringRef.str;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ResettableEditTextPreference;
import app.revanced.extension.shared.ui.CustomDialog;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockSettings;
import app.revanced.extension.youtube.sponsorblock.SponsorBlockUtils;
import app.revanced.extension.youtube.sponsorblock.objects.UserStats;
import app.revanced.extension.youtube.sponsorblock.requests.SBRequester;

/**
 * User skip stats.
 * None of the preferences here show up in search results because
 * a category cannot be added to another category for the search results.
 * Additionally the stats must load remotely on a background thread which means the
 * preferences are not available to collect for search when the settings first load.
 */
@SuppressWarnings({"unused", "deprecation"})
public class SponsorBlockStatsPreferenceCategory extends PreferenceCategory {

    public SponsorBlockStatsPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SponsorBlockStatsPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SponsorBlockStatsPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToActivity() {
        try {
            super.onAttachedToActivity();

            Logger.printDebug(() -> "Updating SB stats UI");
            final boolean enabled = Settings.SB_ENABLED.get();
            setEnabled(enabled);
            removeAll();

            if (!SponsorBlockSettings.userHasSBPrivateId()) {
                // User has never voted or created any segments. Only local stats exist.
                addLocalUserStats();
                return;
            }

            Preference loadingPlaceholderPreference = new Preference(getContext());
            loadingPlaceholderPreference.setEnabled(false);
            addPreference(loadingPlaceholderPreference);

            if (enabled) {
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
            Logger.printException(() -> "onAttachedToActivity failure", ex);
        }
    }

    private void addUserStats(Preference loadingPlaceholder, @Nullable UserStats stats) {
        Utils.verifyOnMainThread();
        try {
            if (stats == null) {
                loadingPlaceholder.setTitle(str("revanced_sb_stats_connection_failure"));
                return;
            }
            removeAll();
            Context context = getContext();

            if (stats.totalSegmentCountIncludingIgnored > 0) {
                // If user has not created any segments, there's no reason to set a username.
                String userName = stats.userName;
                ResettableEditTextPreference preference = new ResettableEditTextPreference(context);
                preference.setTitle(fromHtml(str("revanced_sb_stats_username", userName), FROM_HTML_MODE_COMPACT));
                preference.setSummary(str("revanced_sb_stats_username_change"));
                preference.setText(userName);
                preference.setOnPreferenceChangeListener((preference1, value) -> {
                    Utils.runOnBackgroundThread(() -> {
                        String newUserName = (String) value;
                        String errorMessage = SBRequester.setUsername(newUserName);
                        Utils.runOnMainThread(() -> {
                            if (errorMessage == null) {
                                preference.setTitle(fromHtml(str("revanced_sb_stats_username", newUserName), FROM_HTML_MODE_COMPACT));
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
                preference.setEnabled(Settings.SB_PRIVATE_USER_ID.isAvailable()); // Sync with private user ID setting.
                addPreference(preference);
            }

            {
                // Number of segment submissions (does not include ignored segments).
                Preference preference = new Preference(context);
                String formatted = SponsorBlockUtils.getNumberOfSkipsString(stats.segmentCount);
                preference.setTitle(fromHtml(str("revanced_sb_stats_submissions", formatted), FROM_HTML_MODE_COMPACT));
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
                preference.setEnabled(Settings.SB_ENABLED.isAvailable());
                addPreference(preference);
            }

            {
                // "user reputation".  Usually not useful since it appears most users have zero reputation.
                // But if there is a reputation then show it here.
                Preference preference = new Preference(context);
                preference.setTitle(fromHtml(str("revanced_sb_stats_reputation", stats.reputation), FROM_HTML_MODE_COMPACT));
                preference.setSelectable(false);
                preference.setEnabled(Settings.SB_ENABLED.isAvailable());
                if (stats.reputation != 0) {
                    addPreference(preference);
                }
            }

            {
                // Time saved for other users.
                Preference preference = new Preference(context);

                String stats_saved;
                String stats_saved_sum;
                if (stats.totalSegmentCountIncludingIgnored == 0) {
                    stats_saved = str("revanced_sb_stats_saved_zero");
                    stats_saved_sum = str("revanced_sb_stats_saved_sum_zero");
                } else {
                    stats_saved = str("revanced_sb_stats_saved",
                            SponsorBlockUtils.getNumberOfSkipsString(stats.viewCount));
                    stats_saved_sum = str("revanced_sb_stats_saved_sum",
                            SponsorBlockUtils.getTimeSavedString((long) (60 * stats.minutesSaved)));
                }
                preference.setTitle(fromHtml(stats_saved, FROM_HTML_MODE_COMPACT));
                preference.setSummary(fromHtml(stats_saved_sum, FROM_HTML_MODE_COMPACT));
                preference.setOnPreferenceClickListener(preference1 -> {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://sponsor.ajay.app/stats/"));
                    preference1.getContext().startActivity(i);
                    return false;
                });
                preference.setEnabled(Settings.SB_ENABLED.isAvailable());
                addPreference(preference);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "addUserStats failure", ex);
        }
    }

    private void addLocalUserStats() {
        // Time the user saved by using SB.
        Preference preference = new Preference(getContext());
        Runnable updateStatsSelfSaved = () -> {
            String formatted = SponsorBlockUtils.getNumberOfSkipsString(
                    Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.get());
            preference.setTitle(fromHtml(str("revanced_sb_stats_self_saved", formatted), FROM_HTML_MODE_COMPACT));

            String formattedSaved = SponsorBlockUtils.getTimeSavedString(
                    Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.get() / 1000);
            preference.setSummary(fromHtml(str("revanced_sb_stats_self_saved_sum", formattedSaved), FROM_HTML_MODE_COMPACT));
        };
        updateStatsSelfSaved.run();

        preference.setOnPreferenceClickListener(preference1 -> {
            Pair<Dialog, LinearLayout> dialogPair = CustomDialog.create(
                    preference.getContext(),
                    str("revanced_sb_stats_self_saved_reset_title"), // Title.
                    null, // No message.
                    null, // No EditText.
                    null, // OK button text.
                    () -> {
                        // OK button action.
                        Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.resetToDefault();
                        Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.resetToDefault();
                        updateStatsSelfSaved.run();
                    },
                    () -> {}, // Cancel button action (dismiss only).
                    null, // No neutral button.
                    null, // No neutral button action.
                    true  // Dismiss dialog when onNeutralClick.
            );

            // Show the dialog.
            dialogPair.first.show();
            return true;
        });

        preference.setEnabled(Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.isAvailable());
        addPreference(preference);
    }
}
