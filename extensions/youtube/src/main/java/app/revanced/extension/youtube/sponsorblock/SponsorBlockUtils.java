package app.revanced.extension.youtube.sponsorblock;

import static app.revanced.extension.shared.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.patches.VideoInformation;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.sponsorblock.objects.CategoryBehaviour;
import app.revanced.extension.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.extension.youtube.sponsorblock.objects.SponsorSegment;
import app.revanced.extension.youtube.sponsorblock.objects.SponsorSegment.SegmentVote;
import app.revanced.extension.youtube.sponsorblock.requests.SBRequester;
import app.revanced.extension.youtube.sponsorblock.ui.SponsorBlockViewController;

/**
 * Not thread safe. All fields/methods must be accessed from the main thread.
 */
public class SponsorBlockUtils {
    private static final int LOCKED_COLOR = Color.parseColor("#FFC83D");
    private static final String MANUAL_EDIT_TIME_TEXT_HINT = "hh:mm:ss.sss";
    private static final Pattern manualEditTimePattern
            = Pattern.compile("((\\d{1,2}):)?(\\d{1,2}):(\\d{2})(\\.(\\d{1,3}))?");
    private static final NumberFormat statsNumberFormatter = NumberFormat.getNumberInstance();

    private static long newSponsorSegmentDialogShownMillis;
    private static long newSponsorSegmentStartMillis = -1;
    private static long newSponsorSegmentEndMillis = -1;
    private static boolean newSponsorSegmentPreviewed;
    private static final DialogInterface.OnClickListener newSponsorSegmentDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    // Start.
                    newSponsorSegmentStartMillis = newSponsorSegmentDialogShownMillis;
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    // End.
                    newSponsorSegmentEndMillis = newSponsorSegmentDialogShownMillis;
                    break;
            }
            dialog.dismiss();
        }
    };
    private static SegmentCategory newUserCreatedSegmentCategory;
    private static final DialogInterface.OnClickListener segmentTypeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                SegmentCategory category = SegmentCategory.categoriesWithoutHighlights()[which];
                final boolean enableButton;
                if (category.behaviour == CategoryBehaviour.IGNORE) {
                    Utils.showToastLong(str("revanced_sb_new_segment_disabled_category"));
                    enableButton = false;
                } else {
                    newUserCreatedSegmentCategory = category;
                    enableButton = true;
                }

                ((AlertDialog) dialog)
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .setEnabled(enableButton);
            } catch (Exception ex) {
                Logger.printException(() -> "segmentTypeListener failure", ex);
            }
        }
    };
    private static final DialogInterface.OnClickListener segmentReadyDialogButtonListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                SponsorBlockViewController.hideNewSegmentLayout();
                Context context = ((AlertDialog) dialog).getContext();
                dialog.dismiss();

                SegmentCategory[] categories = SegmentCategory.categoriesWithoutHighlights();
                CharSequence[] titles = new CharSequence[categories.length];
                for (int i = 0, length = categories.length; i < length; i++) {
                    titles[i] = categories[i].getTitleWithColorDot();
                }

                newUserCreatedSegmentCategory = null;
                new AlertDialog.Builder(context)
                        .setTitle(str("revanced_sb_new_segment_choose_category"))
                        .setSingleChoiceItems(titles, -1, segmentTypeListener)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, segmentCategorySelectedDialogListener)
                        .show()
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .setEnabled(false);
            } catch (Exception ex) {
                Logger.printException(() -> "segmentReadyDialogButtonListener failure", ex);
            }
        }
    };
    private static final DialogInterface.OnClickListener segmentCategorySelectedDialogListener = (dialog, which) -> {
        dialog.dismiss();
        submitNewSegment();
    };
    private static final EditByHandSaveDialogListener editByHandSaveDialogListener = new EditByHandSaveDialogListener();
    private static final DialogInterface.OnClickListener editByHandDialogListener = (dialog, which) -> {
        try {
            Context context = ((AlertDialog) dialog).getContext();

            final boolean isStart = DialogInterface.BUTTON_NEGATIVE == which;

            final EditText textView = new EditText(context);
            textView.setHint(MANUAL_EDIT_TIME_TEXT_HINT);
            if (isStart) {
                if (newSponsorSegmentStartMillis >= 0)
                    textView.setText(formatSegmentTime(newSponsorSegmentStartMillis));
            } else {
                if (newSponsorSegmentEndMillis >= 0)
                    textView.setText(formatSegmentTime(newSponsorSegmentEndMillis));
            }

            editByHandSaveDialogListener.settingStart = isStart;
            editByHandSaveDialogListener.editTextRef = new WeakReference<>(textView);
            new AlertDialog.Builder(context)
                    .setTitle(str(isStart ? "revanced_sb_new_segment_time_start" : "revanced_sb_new_segment_time_end"))
                    .setView(textView)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(str("revanced_sb_new_segment_now"), editByHandSaveDialogListener)
                    .setPositiveButton(android.R.string.ok, editByHandSaveDialogListener)
                    .show();

            dialog.dismiss();
        } catch (Exception ex) {
            Logger.printException(() -> "editByHandDialogListener failure", ex);
        }
    };
    private static final DialogInterface.OnClickListener segmentVoteClickListener = (dialog, which) -> {
        try {
            final Context context = ((AlertDialog) dialog).getContext();
            SponsorSegment[] segments = SegmentPlaybackController.getSegments();
            if (segments == null || segments.length == 0) {
                // should never be reached
                Logger.printException(() -> "Segment is no longer available on the client");
                return;
            }
            SponsorSegment segment = segments[which];

            SegmentVote[] voteOptions = (segment.category == SegmentCategory.HIGHLIGHT)
                    ? SegmentVote.voteTypesWithoutCategoryChange // Highlight segments cannot change category.
                    : SegmentVote.values();
            final int voteOptionsLength = voteOptions.length;
            final boolean userIsVip = Settings.SB_USER_IS_VIP.get();
            CharSequence[] items = new CharSequence[voteOptionsLength];

            for (int i = 0; i < voteOptionsLength; i++) {
                SegmentVote voteOption = voteOptions[i];
                CharSequence title = voteOption.title.toString();
                if (userIsVip && segment.isLocked && voteOption.highlightIfVipAndVideoIsLocked) {
                    SpannableString coloredTitle = new SpannableString(title);
                    coloredTitle.setSpan(new ForegroundColorSpan(LOCKED_COLOR),
                            0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    title = coloredTitle;
                }
                items[i] = title;
            }

            new AlertDialog.Builder(context).setItems(items, (dialog1, which1) -> {
                SegmentVote voteOption = voteOptions[which1];
                switch (voteOption) {
                    case UPVOTE:
                    case DOWNVOTE:
                        SBRequester.voteForSegmentOnBackgroundThread(segment, voteOption);
                        break;
                    case CATEGORY_CHANGE:
                        onNewCategorySelect(segment, context);
                        break;
                }
            }).show();
        } catch (Exception ex) {
            Logger.printException(() -> "segmentVoteClickListener failure", ex);
        }
    };

    private SponsorBlockUtils() {
    }

    static void setNewSponsorSegmentPreviewed() {
        newSponsorSegmentPreviewed = true;
    }

    static void clearUnsubmittedSegmentTimes() {
        newSponsorSegmentDialogShownMillis = 0;
        newSponsorSegmentEndMillis = newSponsorSegmentStartMillis = -1;
        newSponsorSegmentPreviewed = false;
    }

    private static void submitNewSegment() {
        try {
            Utils.verifyOnMainThread();
            final long start = newSponsorSegmentStartMillis;
            final long end = newSponsorSegmentEndMillis;
            final String videoId = VideoInformation.getVideoId();
            final long videoLength = VideoInformation.getVideoLength();
            final SegmentCategory segmentCategory = newUserCreatedSegmentCategory;
            if (start < 0 || end < 0 || start >= end || videoLength <= 0 || videoId.isEmpty() || segmentCategory == null) {
                Logger.printException(() -> "invalid parameters");
                return;
            }

            clearUnsubmittedSegmentTimes();
            Utils.runOnBackgroundThread(() -> {
                try {
                    SBRequester.submitSegments(videoId, segmentCategory.keyValue, start, end, videoLength);
                    SegmentPlaybackController.executeDownloadSegments(videoId);
                } catch (Exception ex) {
                    Logger.printException(() -> "submitNewSegment failure", ex);
                }
            });
        } catch (Exception ex) {
            Logger.printException(() -> "submitNewSegment failure", ex);
        }
    }

    public static void onMarkLocationClicked() {
        try {
            Utils.verifyOnMainThread();
            newSponsorSegmentDialogShownMillis = VideoInformation.getVideoTime();

            new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                    .setTitle(str("revanced_sb_new_segment_title"))
                    .setMessage(str("revanced_sb_new_segment_mark_time_as_question",
                            formatSegmentTime(newSponsorSegmentDialogShownMillis)))
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(str("revanced_sb_new_segment_mark_start"), newSponsorSegmentDialogListener)
                    .setPositiveButton(str("revanced_sb_new_segment_mark_end"), newSponsorSegmentDialogListener)
                    .show();
        } catch (Exception ex) {
            Logger.printException(() -> "onMarkLocationClicked failure", ex);
        }
    }

    public static void onPublishClicked() {
        try {
            Utils.verifyOnMainThread();
            if (newSponsorSegmentStartMillis < 0 || newSponsorSegmentEndMillis < 0) {
                Utils.showToastShort(str("revanced_sb_new_segment_mark_locations_first"));
            } else if (newSponsorSegmentStartMillis >= newSponsorSegmentEndMillis) {
                Utils.showToastShort(str("revanced_sb_new_segment_start_is_before_end"));
            } else if (!newSponsorSegmentPreviewed && newSponsorSegmentStartMillis != 0) {
                Utils.showToastLong(str("revanced_sb_new_segment_preview_segment_first"));
            } else {
                final long segmentLength = (newSponsorSegmentEndMillis - newSponsorSegmentStartMillis) / 1000;
                new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                        .setTitle(str("revanced_sb_new_segment_confirm_title"))
                        .setMessage(str("revanced_sb_new_segment_confirm_content",
                                formatSegmentTime(newSponsorSegmentStartMillis),
                                formatSegmentTime(newSponsorSegmentEndMillis),
                                getTimeSavedString(segmentLength)))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, segmentReadyDialogButtonListener)
                        .show();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onPublishClicked failure", ex);
        }
    }

    public static void onVotingClicked(Context context) {
        try {
            Utils.verifyOnMainThread();
            SponsorSegment[] segments = SegmentPlaybackController.getSegments();
            if (segments == null || segments.length == 0) {
                // Button is hidden if no segments exist.
                // But if prior video had segments, and current video does not,
                // then the button persists until the overlay fades out (this is intentional, as abruptly hiding the button is jarring).
                Utils.showToastShort(str("revanced_sb_vote_no_segments"));
                return;
            }

            final int numberOfSegments = segments.length;
            CharSequence[] titles = new CharSequence[numberOfSegments];
            for (int i = 0; i < numberOfSegments; i++) {
                SponsorSegment segment = segments[i];
                if (segment.category == SegmentCategory.UNSUBMITTED) {
                    continue;
                }

                SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                spannableBuilder.append(segment.category.getTitleWithColorDot());
                spannableBuilder.append('\n');

                String startTime = formatSegmentTime(segment.start);
                if (segment.category == SegmentCategory.HIGHLIGHT) {
                    spannableBuilder.append(startTime);
                } else {
                    String toFromString = str("revanced_sb_vote_segment_time_to_from",
                            startTime, formatSegmentTime(segment.end));
                    spannableBuilder.append(toFromString);
                }

                if (i + 1 != numberOfSegments) {
                    // Prevents trailing new line after last segment.
                    spannableBuilder.append('\n');
                }

                spannableBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                        0, spannableBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                titles[i] = spannableBuilder;
            }

            new AlertDialog.Builder(context).setItems(titles, segmentVoteClickListener).show();
        } catch (Exception ex) {
            Logger.printException(() -> "onVotingClicked failure", ex);
        }
    }

    private static void onNewCategorySelect(SponsorSegment segment, Context context) {
        try {
            Utils.verifyOnMainThread();
            final SegmentCategory[] values = SegmentCategory.categoriesWithoutHighlights();
            CharSequence[] titles = new CharSequence[values.length];
            for (int i = 0; i < values.length; i++) {
                titles[i] = values[i].getTitleWithColorDot();
            }

            new AlertDialog.Builder(context)
                    .setTitle(str("revanced_sb_new_segment_choose_category"))
                    .setItems(titles, (dialog, which) -> SBRequester.voteToChangeCategoryOnBackgroundThread(segment, values[which]))
                    .show();
        } catch (Exception ex) {
            Logger.printException(() -> "onNewCategorySelect failure", ex);
        }
    }

    public static void onPreviewClicked() {
        try {
            Utils.verifyOnMainThread();
            if (newSponsorSegmentStartMillis < 0 || newSponsorSegmentEndMillis < 0) {
                Utils.showToastShort(str("revanced_sb_new_segment_mark_locations_first"));
            } else if (newSponsorSegmentStartMillis >= newSponsorSegmentEndMillis) {
                Utils.showToastShort(str("revanced_sb_new_segment_start_is_before_end"));
            } else {
                SegmentPlaybackController.removeUnsubmittedSegments(); // If user hits preview more than once before playing.
                SegmentPlaybackController.addUnsubmittedSegment(
                        new SponsorSegment(SegmentCategory.UNSUBMITTED, null,
                                newSponsorSegmentStartMillis, newSponsorSegmentEndMillis, false));
                VideoInformation.seekTo(newSponsorSegmentStartMillis - 2000);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onPreviewClicked failure", ex);
        }
    }

    static void sendViewRequestAsync(SponsorSegment segment) {
        if (segment.recordedAsSkipped || segment.category == SegmentCategory.UNSUBMITTED) {
            return;
        }
        segment.recordedAsSkipped = true;
        final long totalTimeSkipped = Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.get() + segment.length();
        Settings.SB_LOCAL_TIME_SAVED_MILLISECONDS.save(totalTimeSkipped);
        Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.save(Settings.SB_LOCAL_TIME_SAVED_NUMBER_SEGMENTS.get() + 1);

        if (Settings.SB_TRACK_SKIP_COUNT.get()) {
            Utils.runOnBackgroundThread(() -> SBRequester.sendSegmentSkippedViewedRequest(segment));
        }
    }

    public static void showErrorDialog(String dialogMessage) {
        Utils.runOnMainThreadNowOrLater(() ->
                new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                        .setMessage(dialogMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(false)
                        .show()
        );
    }

    public static void onEditByHandClicked() {
        try {
            Utils.verifyOnMainThread();
            new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                    .setTitle(str("revanced_sb_new_segment_edit_by_hand_title"))
                    .setMessage(str("revanced_sb_new_segment_edit_by_hand_content"))
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(str("revanced_sb_new_segment_mark_start"), editByHandDialogListener)
                    .setPositiveButton(str("revanced_sb_new_segment_mark_end"), editByHandDialogListener)
                    .show();
        } catch (Exception ex) {
            Logger.printException(() -> "onEditByHandClicked failure", ex);
        }
    }

    public static String getNumberOfSkipsString(int viewCount) {
        return statsNumberFormatter.format(viewCount);
    }

    private static long parseSegmentTime(String time) {
        Matcher matcher = manualEditTimePattern.matcher(time);
        if (!matcher.matches()) {
            return -1;
        }
        String hoursStr = matcher.group(2); // Hours is optional.
        String minutesStr = matcher.group(3);
        String secondsStr = matcher.group(4);
        String millisecondsStr = matcher.group(6); // Milliseconds is optional.

        try {
            final int hours = (hoursStr != null) ? Integer.parseInt(hoursStr) : 0;
            //noinspection ConstantConditions
            final int minutes = Integer.parseInt(minutesStr);
            //noinspection ConstantConditions
            final int seconds = Integer.parseInt(secondsStr);
            final int milliseconds;
            if (millisecondsStr != null) {
                // Pad out with zeros if not all decimal places were used.
                millisecondsStr = String.format(Locale.US, "%-3s", millisecondsStr).replace(' ', '0');
                milliseconds = Integer.parseInt(millisecondsStr);
            } else {
                milliseconds = 0;
            }

            return (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + milliseconds;
        } catch (NumberFormatException ex) {
            Logger.printInfo(() -> "Time format exception: " + time, ex);
            return -1;
        }
    }

    private static String formatSegmentTime(long segmentTime) {
        // Use same time formatting as shown in the video player.
        final long videoLength = VideoInformation.getVideoLength();

        // Cannot use DateFormatter, as videos over 24 hours will rollover and not display correctly.
        final long hours = TimeUnit.MILLISECONDS.toHours(segmentTime);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(segmentTime) % 60;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(segmentTime) % 60;
        final long milliseconds = segmentTime % 1000;

        final String formatPattern;
        Object[] formatArgs = {minutes, seconds, milliseconds};

        if (videoLength < (10 * 60 * 1000)) {
            formatPattern = "%01d:%02d.%03d"; // Less than 10 minutes.
        } else if (videoLength < (60 * 60 * 1000)) {
            formatPattern = "%02d:%02d.%03d"; // Less than 1 hour.
        } else if (videoLength < (10 * 60 * 60 * 1000)) {
            formatPattern = "%01d:%02d:%02d.%03d"; // Less than 10 hours.
            formatArgs = new Object[]{hours, minutes, seconds, milliseconds};
        } else {
            formatPattern = "%02d:%02d:%02d.%03d"; // Why is this on YouTube?
            formatArgs = new Object[]{hours, minutes, seconds, milliseconds};
        }

        return String.format(Locale.US, formatPattern, formatArgs);
    }

    public static String getTimeSavedString(long totalSecondsSaved) {
        Duration duration = Duration.ofSeconds(totalSecondsSaved);
        final long hours = duration.toHours();
        final long minutes = duration.toMinutes() % 60;

        // Format all numbers so non-western numbers use a consistent appearance.
        String minutesFormatted = statsNumberFormatter.format(minutes);
        if (hours > 0) {
            String hoursFormatted = statsNumberFormatter.format(hours);
            return str("revanced_sb_stats_saved_hour_format", hoursFormatted, minutesFormatted);
        }

        final long seconds = duration.getSeconds() % 60;
        String secondsFormatted = statsNumberFormatter.format(seconds);
        if (minutes > 0) {
            return str("revanced_sb_stats_saved_minute_format", minutesFormatted, secondsFormatted);
        }

        return str("revanced_sb_stats_saved_second_format", secondsFormatted);
    }

    private static class EditByHandSaveDialogListener implements DialogInterface.OnClickListener {
        private boolean settingStart;
        private WeakReference<EditText> editTextRef = new WeakReference<>(null);

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                final EditText editText = editTextRef.get();
                if (editText == null) return;

                final long time;
                if (which == DialogInterface.BUTTON_NEUTRAL) {
                    time = VideoInformation.getVideoTime();
                } else {
                    time = parseSegmentTime(editText.getText().toString());
                    if (time < 0) {
                        Utils.showToastLong(str("revanced_sb_new_segment_edit_by_hand_parse_error"));
                        return;
                    }
                }

                if (settingStart) {
                    newSponsorSegmentStartMillis = Math.max(time, 0);
                } else {
                    newSponsorSegmentEndMillis = time;
                }

                if (which == DialogInterface.BUTTON_NEUTRAL)
                    editByHandDialogListener.onClick(dialog, settingStart ?
                            DialogInterface.BUTTON_NEGATIVE :
                            DialogInterface.BUTTON_POSITIVE);
            } catch (Exception ex) {
                Logger.printException(() -> "EditByHandSaveDialogListener failure", ex);
            }
        }
    }
}
