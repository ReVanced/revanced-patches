package app.revanced.integrations.youtube.sponsorblock;

import static app.revanced.integrations.shared.StringRef.str;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import app.revanced.integrations.youtube.patches.VideoInformation;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.sponsorblock.objects.CategoryBehaviour;
import app.revanced.integrations.youtube.sponsorblock.objects.SegmentCategory;
import app.revanced.integrations.youtube.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.youtube.sponsorblock.objects.SponsorSegment.SegmentVote;
import app.revanced.integrations.youtube.sponsorblock.requests.SBRequester;
import app.revanced.integrations.youtube.sponsorblock.ui.SponsorBlockViewController;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

/**
 * Not thread safe. All fields/methods must be accessed from the main thread.
 */
public class SponsorBlockUtils {
    private static final String MANUAL_EDIT_TIME_FORMAT = "HH:mm:ss.SSS";
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat manualEditTimeFormatter = new SimpleDateFormat(MANUAL_EDIT_TIME_FORMAT);
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat voteSegmentTimeFormatter = new SimpleDateFormat();
    private static final NumberFormat statsNumberFormatter = NumberFormat.getNumberInstance();
    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        manualEditTimeFormatter.setTimeZone(utc);
        voteSegmentTimeFormatter.setTimeZone(utc);
    }
    private static final String LOCKED_COLOR = "#FFC83D";

    private static long newSponsorSegmentDialogShownMillis;
    private static long newSponsorSegmentStartMillis = -1;
    private static long newSponsorSegmentEndMillis = -1;
    private static boolean newSponsorSegmentPreviewed;
    private static final DialogInterface.OnClickListener newSponsorSegmentDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE:
                    // start
                    newSponsorSegmentStartMillis = newSponsorSegmentDialogShownMillis;
                    break;
                case DialogInterface.BUTTON_POSITIVE:
                    // end
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
                    Utils.showToastLong(str("sb_new_segment_disabled_category"));
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
                        .setTitle(str("sb_new_segment_choose_category"))
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
            textView.setHint(MANUAL_EDIT_TIME_FORMAT);
            if (isStart) {
                if (newSponsorSegmentStartMillis >= 0)
                    textView.setText(manualEditTimeFormatter.format(new Date(newSponsorSegmentStartMillis)));
            } else {
                if (newSponsorSegmentEndMillis >= 0)
                    textView.setText(manualEditTimeFormatter.format(new Date(newSponsorSegmentEndMillis)));
            }

            editByHandSaveDialogListener.settingStart = isStart;
            editByHandSaveDialogListener.editText = new WeakReference<>(textView);
            new AlertDialog.Builder(context)
                    .setTitle(str(isStart ? "sb_new_segment_time_start" : "sb_new_segment_time_end"))
                    .setView(textView)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton(str("sb_new_segment_now"), editByHandSaveDialogListener)
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
                    ? SegmentVote.voteTypesWithoutCategoryChange // highlight segments cannot change category
                    : SegmentVote.values();
            CharSequence[] items = new CharSequence[voteOptions.length];

            for (int i = 0; i < voteOptions.length; i++) {
                SegmentVote voteOption = voteOptions[i];
                String title = voteOption.title.toString();
                if (Settings.SB_USER_IS_VIP.get() && segment.isLocked && voteOption.shouldHighlight) {
                    items[i] = Html.fromHtml(String.format("<font color=\"%s\">%s</font>", LOCKED_COLOR, title));
                } else {
                    items[i] = title;
                }
            }

            new AlertDialog.Builder(context)
                    .setItems(items, (dialog1, which1) -> {
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
                    })
                    .show();
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
                SBRequester.submitSegments(videoId, segmentCategory.keyValue, start, end, videoLength);
                SegmentPlaybackController.executeDownloadSegments(videoId);
            });
        } catch (Exception e) {
            Logger.printException(() -> "Unable to submit segment", e);
        }
    }

    public static void onMarkLocationClicked() {
        try {
            Utils.verifyOnMainThread();
            newSponsorSegmentDialogShownMillis = VideoInformation.getVideoTime();

            new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                    .setTitle(str("sb_new_segment_title"))
                    .setMessage(str("sb_new_segment_mark_time_as_question",
                            newSponsorSegmentDialogShownMillis / 60000,
                            newSponsorSegmentDialogShownMillis / 1000 % 60,
                            newSponsorSegmentDialogShownMillis % 1000))
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(str("sb_new_segment_mark_start"), newSponsorSegmentDialogListener)
                    .setPositiveButton(str("sb_new_segment_mark_end"), newSponsorSegmentDialogListener)
                    .show();
        } catch (Exception ex) {
            Logger.printException(() -> "onMarkLocationClicked failure", ex);
        }
    }

    public static void onPublishClicked() {
        try {
            Utils.verifyOnMainThread();
            if (newSponsorSegmentStartMillis < 0 || newSponsorSegmentEndMillis < 0) {
                Utils.showToastShort(str("sb_new_segment_mark_locations_first"));
            } else if (newSponsorSegmentStartMillis >= newSponsorSegmentEndMillis) {
                Utils.showToastShort(str("sb_new_segment_start_is_before_end"));
            } else if (!newSponsorSegmentPreviewed && newSponsorSegmentStartMillis != 0) {
                Utils.showToastLong(str("sb_new_segment_preview_segment_first"));
            } else {
                long length = (newSponsorSegmentEndMillis - newSponsorSegmentStartMillis) / 1000;
                long start = (newSponsorSegmentStartMillis) / 1000;
                long end = (newSponsorSegmentEndMillis) / 1000;
                new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                        .setTitle(str("sb_new_segment_confirm_title"))
                        .setMessage(str("sb_new_segment_confirm_content",
                                start / 60, start % 60,
                                end / 60, end % 60,
                                length / 60, length % 60))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, segmentReadyDialogButtonListener)
                        .show();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onPublishClicked failure", ex);
        }
    }

    public static void onVotingClicked(@NonNull Context context) {
        try {
            Utils.verifyOnMainThread();
            SponsorSegment[] segments = SegmentPlaybackController.getSegments();
            if (segments == null || segments.length == 0) {
                // Button is hidden if no segments exist.
                // But if prior video had segments, and current video does not,
                // then the button persists until the overlay fades out (this is intentional, as abruptly hiding the button is jarring).
                Utils.showToastShort(str("sb_vote_no_segments"));
                return;
            }

            // use same time formatting as shown in the video player
            final long videoLength = VideoInformation.getVideoLength();
            final String formatPattern;
            if (videoLength < (10 * 60 * 1000)) {
                formatPattern = "m:ss.SSS"; // less than 10 minutes
            } else if (videoLength < (60 * 60 * 1000)) {
                formatPattern = "mm:ss.SSS"; // less than 1 hour
            } else if (videoLength < (10 * 60 * 60 * 1000)) {
                formatPattern = "H:mm:ss.SSS"; // less than 10 hours
            } else {
                formatPattern = "HH:mm:ss.SSS"; // why is this on YouTube
            }
            voteSegmentTimeFormatter.applyPattern(formatPattern);

            final int numberOfSegments = segments.length;
            CharSequence[] titles = new CharSequence[numberOfSegments];
            for (int i = 0; i < numberOfSegments; i++) {
                SponsorSegment segment = segments[i];
                if (segment.category == SegmentCategory.UNSUBMITTED) {
                    continue;
                }
                StringBuilder htmlBuilder = new StringBuilder();
                htmlBuilder.append(String.format("<b><font color=\"#%06X\">⬤</font> %s<br>",
                        segment.category.color, segment.category.title));
                htmlBuilder.append(voteSegmentTimeFormatter.format(new Date(segment.start)));
                if (segment.category != SegmentCategory.HIGHLIGHT) {
                    htmlBuilder.append(" to ").append(voteSegmentTimeFormatter.format(new Date(segment.end)));
                }
                htmlBuilder.append("</b>");
                if (i + 1 != numberOfSegments) // prevents trailing new line after last segment
                    htmlBuilder.append("<br>");
                titles[i] = Html.fromHtml(htmlBuilder.toString());
            }

            new AlertDialog.Builder(context)
                    .setItems(titles, segmentVoteClickListener)
                    .show();
        } catch (Exception ex) {
            Logger.printException(() -> "onVotingClicked failure", ex);
        }
    }

    private static void onNewCategorySelect(@NonNull SponsorSegment segment, @NonNull Context context) {
        try {
            Utils.verifyOnMainThread();
            final SegmentCategory[] values = SegmentCategory.categoriesWithoutHighlights();
            CharSequence[] titles = new CharSequence[values.length];
            for (int i = 0; i < values.length; i++) {
                titles[i] = values[i].getTitleWithColorDot();
            }

            new AlertDialog.Builder(context)
                    .setTitle(str("sb_new_segment_choose_category"))
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
                Utils.showToastShort(str("sb_new_segment_mark_locations_first"));
            } else if (newSponsorSegmentStartMillis >= newSponsorSegmentEndMillis) {
                Utils.showToastShort(str("sb_new_segment_start_is_before_end"));
            } else {
                SegmentPlaybackController.removeUnsubmittedSegments(); // If user hits preview more than once before playing.
                SegmentPlaybackController.addUnsubmittedSegment(
                        new SponsorSegment(SegmentCategory.UNSUBMITTED, null,
                                newSponsorSegmentStartMillis, newSponsorSegmentEndMillis, false));
                VideoInformation.seekTo(newSponsorSegmentStartMillis - 2500);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onPreviewClicked failure", ex);
        }
    }


    static void sendViewRequestAsync(@NonNull SponsorSegment segment) {
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

    public static void onEditByHandClicked() {
        try {
            Utils.verifyOnMainThread();
            new AlertDialog.Builder(SponsorBlockViewController.getOverLaysViewGroupContext())
                    .setTitle(str("sb_new_segment_edit_by_hand_title"))
                    .setMessage(str("sb_new_segment_edit_by_hand_content"))
                    .setNeutralButton(android.R.string.cancel, null)
                    .setNegativeButton(str("sb_new_segment_mark_start"), editByHandDialogListener)
                    .setPositiveButton(str("sb_new_segment_mark_end"), editByHandDialogListener)
                    .show();
        } catch (Exception ex) {
            Logger.printException(() -> "onEditByHandClicked failure", ex);
        }
    }

    public static String getNumberOfSkipsString(int viewCount) {
        return statsNumberFormatter.format(viewCount);
    }

    public static String getTimeSavedString(long totalSecondsSaved) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Duration duration = Duration.ofSeconds(totalSecondsSaved);
            final long hours = duration.toHours();
            final long minutes = duration.toMinutes() % 60;
            // Format all numbers so non-western numbers use a consistent appearance.
            String minutesFormatted = statsNumberFormatter.format(minutes);
            if (hours > 0) {
                String hoursFormatted = statsNumberFormatter.format(hours);
                return str("sb_stats_saved_hour_format", hoursFormatted, minutesFormatted);
            }
            final long seconds = duration.getSeconds() % 60;
            String secondsFormatted = statsNumberFormatter.format(seconds);
            if (minutes > 0) {
                return str("sb_stats_saved_minute_format", minutesFormatted, secondsFormatted);
            }
            return str("sb_stats_saved_second_format", secondsFormatted);
        }
        return "error"; // will never be reached.  YouTube requires Android O or greater
    }

    private static class EditByHandSaveDialogListener implements DialogInterface.OnClickListener {
        boolean settingStart;
        WeakReference<EditText> editText;

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                final EditText editText = this.editText.get();
                if (editText == null) return;

                long time = (which == DialogInterface.BUTTON_NEUTRAL) ?
                        VideoInformation.getVideoTime() :
                        (Objects.requireNonNull(manualEditTimeFormatter.parse(editText.getText().toString())).getTime());

                if (settingStart)
                    newSponsorSegmentStartMillis = Math.max(time, 0);
                else
                    newSponsorSegmentEndMillis = time;

                if (which == DialogInterface.BUTTON_NEUTRAL)
                    editByHandDialogListener.onClick(dialog, settingStart ?
                            DialogInterface.BUTTON_NEGATIVE :
                            DialogInterface.BUTTON_POSITIVE);
            } catch (ParseException e) {
                Utils.showToastLong(str("sb_new_segment_edit_by_hand_parse_error"));
            } catch (Exception ex) {
                Logger.printException(() -> "EditByHandSaveDialogListener failure", ex);
            }
        }
    }
}
