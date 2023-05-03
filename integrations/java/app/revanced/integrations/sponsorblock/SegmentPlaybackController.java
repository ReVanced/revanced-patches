package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.utils.StringRef.str;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.CategoryBehaviour;
import app.revanced.integrations.sponsorblock.objects.SegmentCategory;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.sponsorblock.ui.SponsorBlockViewController;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Handles showing, scheduling, and skipping of all {@link SponsorSegment} for the current video.
 *
 * Class is not thread safe. All methods must be called on the main thread unless otherwise specified.
 */
public class SegmentPlaybackController {
    /**
     * Length of time to show a skip button for a highlight segment,
     * or a regular segment if {@link SettingsEnum#SB_AUTO_HIDE_SKIP_BUTTON} is enabled.
     *
     * Because Effectively, this value is rounded up to the next second.
     */
    private static final long DURATION_TO_SHOW_SKIP_BUTTON = 3800;

    /*
     * Highlight segments have zero length, as they are a point in time.
     * Draw them on screen using a fixed width bar.
     * Value is independent of device dpi.
     */
    private static final int HIGHLIGHT_SEGMENT_DRAW_BAR_WIDTH = 7;

    @Nullable
    private static String currentVideoId;
    @Nullable
    private static SponsorSegment[] segments;

    /**
     * Highlight segment, if one exists.
     */
    @Nullable
    private static SponsorSegment highlightSegment;

    /**
     * Because loading can take time, show the skip to highlight for a few seconds after the segments load.
     * This is the system time (in milliseconds) to no longer show the initial display skip to highlight.
     */
    private static long highlightSegmentInitialShowEndTime;

    /**
     * Currently playing (non-highlight) segment that user can manually skip.
     */
    @Nullable
    private static SponsorSegment segmentCurrentlyPlaying;
    /**
     * Currently playing manual skip segment that is scheduled to hide.
     * This will always be NULL or equal to {@link #segmentCurrentlyPlaying}.
     */
    @Nullable
    private static SponsorSegment scheduledHideSegment;
    /**
     * Upcoming segment that is scheduled to either autoskip or show the manual skip button.
     */
    @Nullable
    private static SponsorSegment scheduledUpcomingSegment;

    /**
     * Used to prevent re-showing a previously hidden skip button when exiting an embedded segment.
     * Only used when {@link SettingsEnum#SB_AUTO_HIDE_SKIP_BUTTON} is enabled.
     *
     * A collection of segments that have automatically hidden the skip button for, and all segments in this list
     * contain the current video time.  Segment are removed when playback exits the segment.
     */
    private static final List<SponsorSegment> hiddenSkipSegmentsForCurrentVideoTime = new ArrayList<>();

    /**
     * System time (in milliseconds) of when to hide the skip button of {@link #segmentCurrentlyPlaying}.
     * Value is zero if playback is not inside a segment ({@link #segmentCurrentlyPlaying} is null),
     * or if {@link SettingsEnum#SB_AUTO_HIDE_SKIP_BUTTON} is not enabled.
     */
    private static long skipSegmentButtonEndTime;

    @Nullable
    private static String timeWithoutSegments;

    private static int sponsorBarAbsoluteLeft;
    private static int sponsorAbsoluteBarRight;
    private static int sponsorBarThickness;

    @Nullable
    static SponsorSegment[] getSegments() {
        return segments;
    }

    private static void setSegments(@NonNull SponsorSegment[] videoSegments) {
        Arrays.sort(videoSegments);
        segments = videoSegments;
        calculateTimeWithoutSegments();

        for (SponsorSegment segment : videoSegments) {
            if (segment.category == SegmentCategory.HIGHLIGHT) {
                highlightSegment = segment;
                return;
            }
        }
        highlightSegment = null;
    }

    static void addUnsubmittedSegment(@NonNull SponsorSegment segment) {
        Objects.requireNonNull(segment);
        if (segments == null) {
            segments = new SponsorSegment[1];
        } else {
            segments = Arrays.copyOf(segments, segments.length + 1);
        }
        segments[segments.length - 1] = segment;
        setSegments(segments);
    }

    static void removeUnsubmittedSegments() {
        if (segments == null || segments.length == 0) {
            return;
        }
        List<SponsorSegment> replacement = new ArrayList<>();
        for (SponsorSegment segment : segments) {
            if (segment.category != SegmentCategory.UNSUBMITTED) {
                replacement.add(segment);
            }
        }
        if (replacement.size() != segments.length) {
            setSegments(replacement.toArray(new SponsorSegment[0]));
        }
    }

    public static boolean videoHasSegments() {
        return segments != null && segments.length > 0;
    }

    /**
     * Clears all downloaded data.
     */
    private static void clearData() {
        currentVideoId = null;
        segments = null;
        highlightSegment = null;
        highlightSegmentInitialShowEndTime = 0;
        timeWithoutSegments = null;
        segmentCurrentlyPlaying = null;
        scheduledUpcomingSegment = null;
        scheduledHideSegment = null;
        skipSegmentButtonEndTime = 0;
        toastSegmentSkipped = null;
        toastNumberOfSegmentsSkipped = 0;
        hiddenSkipSegmentsForCurrentVideoTime.clear();
    }

    /**
     * Injection point.
     * Initializes SponsorBlock when the video player starts playing a new video.
     */
    public static void initialize(Object _o) {
        try {
            ReVancedUtils.verifyOnMainThread();
            SponsorBlockSettings.initialize();
            clearData();
            SponsorBlockViewController.hideAll();
            SponsorBlockUtils.clearUnsubmittedSegmentTimes();
            LogHelper.printDebug(() -> "Initialized SponsorBlock");
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to initialize SponsorBlock", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void setCurrentVideoId(@Nullable String videoId) {
        try {
            if (Objects.equals(currentVideoId, videoId)) {
                return;
            }
            clearData();
            if (videoId == null || !SettingsEnum.SB_ENABLED.getBoolean()) {
                return;
            }
            if (PlayerType.getCurrent().isNoneOrHidden()) {
                LogHelper.printDebug(() -> "ignoring short or story");
                return;
            }
            if (!ReVancedUtils.isNetworkConnected()) {
                LogHelper.printDebug(() -> "Network not connected, ignoring video");
                return;
            }

            currentVideoId = videoId;
            LogHelper.printDebug(() -> "setCurrentVideoId: " + videoId);

            ReVancedUtils.runOnBackgroundThread(() -> {
                try {
                    executeDownloadSegments(videoId);
                } catch (Exception e) {
                    LogHelper.printException(() -> "Failed to download segments", e);
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "setCurrentVideoId failure", ex);
        }
    }

    /**
     * Must be called off main thread
     */
    static void executeDownloadSegments(@NonNull String videoId) {
        Objects.requireNonNull(videoId);
        try {
            SponsorSegment[] segments = SBRequester.getSegments(videoId);

            ReVancedUtils.runOnMainThread(()-> {
                if (!videoId.equals(currentVideoId)) {
                    // user changed videos before get segments network call could complete
                    LogHelper.printDebug(() -> "Ignoring segments for prior video: " + videoId);
                    return;
                }
                setSegments(segments);

                final long videoTime = VideoInformation.getVideoTime();
                // if the current video time is before the highlight
                if (highlightSegment != null && videoTime < highlightSegment.end) {
                    if (highlightSegment.shouldAutoSkip()) {
                        skipSegment(highlightSegment, false);
                        return;
                    }
                    highlightSegmentInitialShowEndTime = System.currentTimeMillis() + DURATION_TO_SHOW_SKIP_BUTTON;
                }
                // check for any skips now, instead of waiting for the next update to setVideoTime()
                setVideoTime(videoTime);
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "executeDownloadSegments failure", ex);
        }
    }

    /**
     * Injection point.
     * Updates SponsorBlock every 1000ms.
     * When changing videos, this is first called with value 0 and then the video is changed.
     */
    public static void setVideoTime(long millis) {
        try {
            if (!SettingsEnum.SB_ENABLED.getBoolean()
                || PlayerType.getCurrent().isNoneOrHidden() // shorts playback
                || segments == null || segments.length == 0) {
                return;
            }
            LogHelper.printDebug(() -> "setVideoTime: " + millis);

            updateHiddenSegments(millis);

            // to debug the timing logic, set this to a very large value (5000 or more)
            // then try manually seeking just playback reaches a skip/hide of different segments
            final long lookAheadMilliseconds = 1500; // must be larger than the average time between calls to this method
            final float playbackSpeed = VideoInformation.getPlaybackSpeed();
            final long startTimerLookAheadThreshold = millis + (long)(playbackSpeed * lookAheadMilliseconds);

            SponsorSegment foundSegmentCurrentlyPlaying = null;
            SponsorSegment foundUpcomingSegment = null;

            for (final SponsorSegment segment : segments) {
                if (segment.category.behaviour == CategoryBehaviour.SHOW_IN_SEEKBAR
                    || segment.category.behaviour == CategoryBehaviour.IGNORE
                    || segment.category == SegmentCategory.HIGHLIGHT) {
                    continue;
                }
                if (segment.end <= millis) {
                    continue; // past this segment
                }

                if (segment.start <= millis) {
                    // we are in the segment!
                    if (segment.shouldAutoSkip()) {
                        skipSegment(segment, false);
                        return; // must return, as skipping causes a recursive call back into this method
                    }

                    // first found segment, or it's an embedded segment and fully inside the outer segment
                    if (foundSegmentCurrentlyPlaying == null || foundSegmentCurrentlyPlaying.containsSegment(segment)) {
                        // If the found segment is not currently displayed, then do not show if the segment is nearly over.
                        // This check prevents the skip button text from rapidly changing when multiple segments end at nearly the same time.
                        // Also prevents showing the skip button if user seeks into the last 800ms of the segment.
                        final long minMillisOfSegmentRemainingThreshold = 800;
                        if (segmentCurrentlyPlaying == segment
                                || !segment.endIsNear(millis, minMillisOfSegmentRemainingThreshold)) {
                            foundSegmentCurrentlyPlaying = segment;
                        } else {
                            LogHelper.printDebug(() -> "Ignoring segment that ends very soon: " + segment);
                        }
                    }
                    // Keep iterating and looking. There may be an upcoming autoskip,
                    // or there may be another smaller segment nested inside this segment
                    continue;
                }

                // segment is upcoming
                if (startTimerLookAheadThreshold < segment.start) {
                    break; // segment is not close enough to schedule, and no segments after this are of interest
                }
                if (segment.shouldAutoSkip()) { // upcoming autoskip
                    foundUpcomingSegment = segment;
                    break; // must stop here
                }

                // upcoming manual skip

                // do not schedule upcoming segment, if it is not fully contained inside the current segment
                if ((foundSegmentCurrentlyPlaying == null || foundSegmentCurrentlyPlaying.containsSegment(segment))
                     // use the most inner upcoming segment
                     && (foundUpcomingSegment == null || foundUpcomingSegment.containsSegment(segment))) {

                    // Only schedule, if the segment start time is not near the end time of the current segment.
                    // This check is needed to prevent scheduled hide and show from clashing with each other.
                    // Instead the upcoming segment will be handled when the current segment scheduled hide calls back into this method.
                    final long minTimeBetweenStartEndOfSegments = 1000;
                    if (foundSegmentCurrentlyPlaying == null
                            || !foundSegmentCurrentlyPlaying.endIsNear(segment.start, minTimeBetweenStartEndOfSegments)) {
                        foundUpcomingSegment = segment;
                    } else {
                        LogHelper.printDebug(() -> "Not scheduling segment (start time is near end of current segment): " + segment);
                    }
                }
            }

            if (highlightSegment != null) {
                if (millis < DURATION_TO_SHOW_SKIP_BUTTON || System.currentTimeMillis() < highlightSegmentInitialShowEndTime) {
                    SponsorBlockViewController.showSkipHighlightButton(highlightSegment);
                } else {
                    SponsorBlockViewController.hideSkipHighlightButton();
                }
            }

            if (segmentCurrentlyPlaying != foundSegmentCurrentlyPlaying) {
                setSegmentCurrentlyPlaying(foundSegmentCurrentlyPlaying);
            } else if (foundSegmentCurrentlyPlaying != null
                    && skipSegmentButtonEndTime != 0 && skipSegmentButtonEndTime <= System.currentTimeMillis()) {
                LogHelper.printDebug(() -> "Auto hiding skip button for segment: " + segmentCurrentlyPlaying);
                skipSegmentButtonEndTime = 0;
                hiddenSkipSegmentsForCurrentVideoTime.add(foundSegmentCurrentlyPlaying);
                SponsorBlockViewController.hideSkipSegmentButton();
            }

            // must be greater than the average time between updates to VideoInformation time
            final long videoInformationTimeUpdateThresholdMilliseconds = 250;

            // schedule a hide, only if the segment end is near
            final SponsorSegment segmentToHide =
                    (foundSegmentCurrentlyPlaying != null && foundSegmentCurrentlyPlaying.endIsNear(millis, lookAheadMilliseconds))
                    ? foundSegmentCurrentlyPlaying
                    : null;

            if (scheduledHideSegment != segmentToHide) {
                if (segmentToHide == null) {
                    LogHelper.printDebug(() -> "Clearing scheduled hide: " + scheduledHideSegment);
                    scheduledHideSegment = null;
                } else {
                    scheduledHideSegment = segmentToHide;
                    LogHelper.printDebug(() -> "Scheduling hide segment: " + segmentToHide + " playbackSpeed: " + playbackSpeed);
                    final long delayUntilHide = (long) ((segmentToHide.end - millis) / playbackSpeed);
                    ReVancedUtils.runOnMainThreadDelayed(() -> {
                        if (scheduledHideSegment != segmentToHide) {
                            LogHelper.printDebug(() -> "Ignoring old scheduled hide segment: " + segmentToHide);
                            return;
                        }
                        scheduledHideSegment = null;

                        final long videoTime = VideoInformation.getVideoTime();
                        if (!segmentToHide.endIsNear(videoTime, videoInformationTimeUpdateThresholdMilliseconds)) {
                            // current video time is not what's expected.  User paused playback
                            LogHelper.printDebug(() -> "Ignoring outdated scheduled hide: " + segmentToHide
                                    + " videoInformation time: " + videoTime);
                            return;
                        }
                        LogHelper.printDebug(() -> "Running scheduled hide segment: " + segmentToHide);
                        // Need more than just hide the skip button, as this may have been an embedded segment
                        // Instead call back into setVideoTime to check everything again.
                        // Should not use VideoInformation time as it is less accurate,
                        // but this scheduled handler was scheduled precisely so we can just use the segment end time
                        setSegmentCurrentlyPlaying(null);
                        setVideoTime(segmentToHide.end);
                    }, delayUntilHide);
                }
            }

            if (scheduledUpcomingSegment != foundUpcomingSegment) {
                if (foundUpcomingSegment == null) {
                    LogHelper.printDebug(() -> "Clearing scheduled segment: " + scheduledUpcomingSegment);
                    scheduledUpcomingSegment = null;
                } else {
                    scheduledUpcomingSegment = foundUpcomingSegment;
                    final SponsorSegment segmentToSkip = foundUpcomingSegment;

                    LogHelper.printDebug(() -> "Scheduling segment: " + segmentToSkip + " playbackSpeed: " + playbackSpeed);
                    final long delayUntilSkip = (long) ((segmentToSkip.start - millis) / playbackSpeed);
                    ReVancedUtils.runOnMainThreadDelayed(() -> {
                        if (scheduledUpcomingSegment != segmentToSkip) {
                            LogHelper.printDebug(() -> "Ignoring old scheduled segment: " + segmentToSkip);
                            return;
                        }
                        scheduledUpcomingSegment = null;

                        final long videoTime = VideoInformation.getVideoTime();
                        if (!segmentToSkip.startIsNear(videoTime,
                                videoInformationTimeUpdateThresholdMilliseconds)) {
                            // current video time is not what's expected.  User paused playback
                            LogHelper.printDebug(() -> "Ignoring outdated scheduled segment: " + segmentToSkip
                                    + " videoInformation time: " + videoTime);
                            return;
                        }
                        if (segmentToSkip.shouldAutoSkip()) {
                            LogHelper.printDebug(() -> "Running scheduled skip segment: " + segmentToSkip);
                            skipSegment(segmentToSkip, false);
                        } else {
                            LogHelper.printDebug(() -> "Running scheduled show segment: " + segmentToSkip);
                            setSegmentCurrentlyPlaying(segmentToSkip);
                        }
                    }, delayUntilSkip);
                }
            }
        } catch (Exception e) {
            LogHelper.printException(() -> "setVideoTime failure", e);
        }
    }

    /**
     * Removes all previously hidden segments that are not longer contained in the given video time.
     */
    private static void updateHiddenSegments(long currentVideoTime) {
        Iterator<SponsorSegment> i = hiddenSkipSegmentsForCurrentVideoTime.iterator();
        while (i.hasNext()) {
            SponsorSegment hiddenSegment = i.next();
            if (!hiddenSegment.containsTime(currentVideoTime)) {
                LogHelper.printDebug(() -> "Resetting hide skip button: " + hiddenSegment);
                i.remove();
            }
        }
    }

    private static void setSegmentCurrentlyPlaying(@Nullable SponsorSegment segment) {
        if (segment == null) {
            if (segmentCurrentlyPlaying != null) LogHelper.printDebug(() -> "Hiding segment: " + segmentCurrentlyPlaying);
            segmentCurrentlyPlaying = null;
            skipSegmentButtonEndTime = 0;
            SponsorBlockViewController.hideSkipSegmentButton();
            return;
        }
        segmentCurrentlyPlaying = segment;
        skipSegmentButtonEndTime = 0;
        if (SettingsEnum.SB_AUTO_HIDE_SKIP_BUTTON.getBoolean()) {
            if (hiddenSkipSegmentsForCurrentVideoTime.contains(segment)) {
                // Playback exited a nested segment and the outer segment skip button was previously hidden.
                LogHelper.printDebug(() -> "Ignoring previously auto-hidden segment: " + segment);
                SponsorBlockViewController.hideSkipSegmentButton();
                return;
            }
            skipSegmentButtonEndTime = System.currentTimeMillis() + DURATION_TO_SHOW_SKIP_BUTTON;
        }
        LogHelper.printDebug(() -> "Showing segment: " + segment);
        SponsorBlockViewController.showSkipSegmentButton(segment);
    }

    private static SponsorSegment lastSegmentSkipped;
    private static long lastSegmentSkippedTime;

    private static void skipSegment(@NonNull SponsorSegment segmentToSkip, boolean userManuallySkipped) {
        try {
            SponsorBlockViewController.hideSkipHighlightButton();
            SponsorBlockViewController.hideSkipSegmentButton();

            // If trying to seek to end of the video, YouTube can seek just short of the actual end.
            // (especially if the video does not end on a whole second boundary).
            // This causes additional segment skip attempts, even though it cannot seek any closer to the desired time.
            // Check for and ignore repeated skip attempts of the same segment over a short time period.
            final long now = System.currentTimeMillis();
            final long minimumMillisecondsBetweenSkippingSameSegment = 500;
            if ((lastSegmentSkipped == segmentToSkip) && (now - lastSegmentSkippedTime < minimumMillisecondsBetweenSkippingSameSegment)) {
                LogHelper.printDebug(() -> "Ignoring skip segment request (already skipped as close as possible): " + segmentToSkip);
                return;
            }

            LogHelper.printDebug(() -> "Skipping segment: " + segmentToSkip);
            lastSegmentSkipped = segmentToSkip;
            lastSegmentSkippedTime = now;
            setSegmentCurrentlyPlaying(null);
            scheduledHideSegment = null;
            scheduledUpcomingSegment = null;
            if (segmentToSkip == highlightSegment) {
                highlightSegmentInitialShowEndTime = 0;
            }

            // If the seek is successful, then the seek causes a recursive call back into this class.
            final boolean seekSuccessful = VideoInformation.seekTo(segmentToSkip.end);
            if (!seekSuccessful) {
                // can happen when switching videos and is normal
                LogHelper.printDebug(() -> "Could not skip segment (seek unsuccessful): " + segmentToSkip);
                return;
            }

            if (!userManuallySkipped) {
                // check for any smaller embedded segments, and count those as autoskipped
                final boolean showSkipToast = SettingsEnum.SB_SHOW_TOAST_ON_SKIP.getBoolean();
                for (final SponsorSegment otherSegment : Objects.requireNonNull(segments)) {
                    if (segmentToSkip.end < otherSegment.start) {
                        break; // no other segments can be contained
                    }
                    if (otherSegment == segmentToSkip ||
                            (otherSegment.category != SegmentCategory.HIGHLIGHT && segmentToSkip.containsSegment(otherSegment))) {
                        otherSegment.didAutoSkipped = true;
                        if (showSkipToast) {
                            showSkippedSegmentToast(otherSegment);
                        }
                    }
                }
            }

            if (segmentToSkip.category == SegmentCategory.UNSUBMITTED) {
                removeUnsubmittedSegments();
                SponsorBlockUtils.setNewSponsorSegmentPreviewed();
            } else {
                SponsorBlockUtils.sendViewRequestAsync(segmentToSkip);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "skipSegment failure", ex);
        }
    }


    private static int toastNumberOfSegmentsSkipped;
    @Nullable
    private static SponsorSegment toastSegmentSkipped;

    private static void showSkippedSegmentToast(@NonNull SponsorSegment segment) {
        ReVancedUtils.verifyOnMainThread();
        toastNumberOfSegmentsSkipped++;
        if (toastNumberOfSegmentsSkipped > 1) {
            return; // toast already scheduled
        }
        toastSegmentSkipped = segment;

        final long delayToToastMilliseconds = 250; // also the maximum time between skips to be considered skipping multiple segments
        ReVancedUtils.runOnMainThreadDelayed(() -> {
            try {
                if (toastSegmentSkipped == null) { // video was changed just after skipping segment
                    LogHelper.printDebug(() -> "Ignoring old scheduled show toast");
                    return;
                }
                ReVancedUtils.showToastShort(toastNumberOfSegmentsSkipped == 1
                        ? toastSegmentSkipped.getSkippedToastText()
                        : str("sb_skipped_multiple_segments"));
            } catch (Exception ex) {
                LogHelper.printException(() -> "showSkippedSegmentToast failure", ex);
            } finally {
                toastNumberOfSegmentsSkipped = 0;
                toastSegmentSkipped = null;
            }
        }, delayToToastMilliseconds);
    }

    /**
     * @param segment can be either a highlight or a regular manual skip segment.
     */
    public static void onSkipSegmentClicked(@NonNull SponsorSegment segment) {
        try {
            if (segment != highlightSegment && segment != segmentCurrentlyPlaying) {
                LogHelper.printException(() -> "error: segment not available to skip"); // should never happen
                SponsorBlockViewController.hideSkipSegmentButton();
                SponsorBlockViewController.hideSkipHighlightButton();
                return;
            }
            skipSegment(segment, true);
        } catch (Exception ex) {
            LogHelper.printException(() -> "onSkipSegmentClicked failure", ex);
        }
    }

    /**
     * Injection point
     */
    public static void setSponsorBarRect(final Object self) {
        try {
            Field field = self.getClass().getDeclaredField("replaceMeWithsetSponsorBarRect");
            field.setAccessible(true);
            Rect rect = (Rect) Objects.requireNonNull(field.get(self));
            setSponsorBarAbsoluteLeft(rect);
            setSponsorBarAbsoluteRight(rect);
        } catch (Exception ex) {
            LogHelper.printException(() -> "setSponsorBarRect failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void setSponsorBarAbsoluteLeft(Rect rect) {
        final int left = rect.left;
        if (sponsorBarAbsoluteLeft != left) {
            LogHelper.printDebug(() -> "setSponsorBarAbsoluteLeft: " + left);
            sponsorBarAbsoluteLeft = left;
        }
    }

    /**
     * Injection point.
     */
    public static void setSponsorBarAbsoluteRight(Rect rect) {
        final int right = rect.right;
        if (sponsorAbsoluteBarRight != right) {
            LogHelper.printDebug(() -> "setSponsorBarAbsoluteRight: " +  right);
            sponsorAbsoluteBarRight = right;
        }
    }

    /**
     * Injection point
     */
    public static void setSponsorBarThickness(int thickness) {
        if (sponsorBarThickness != thickness) {
            LogHelper.printDebug(() -> "setSponsorBarThickness: " + thickness);
            sponsorBarThickness = thickness;
        }
    }

    /**
     * Injection point.
     */
    public static String appendTimeWithoutSegments(String totalTime) {
        try {
            if (SettingsEnum.SB_ENABLED.getBoolean() && SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean()
                    && !TextUtils.isEmpty(totalTime) && !TextUtils.isEmpty(timeWithoutSegments)) {
                // Force LTR layout, to match the same LTR video time/length layout YouTube uses for all languages
                return "\u202D" + totalTime + timeWithoutSegments; // u202D = left to right override
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "appendTimeWithoutSegments failure", ex);
        }

        return totalTime;
    }

    private static void calculateTimeWithoutSegments() {
        final long currentVideoLength = VideoInformation.getVideoLength();
        if (!SettingsEnum.SB_SHOW_TIME_WITHOUT_SEGMENTS.getBoolean() || currentVideoLength <= 0
                || segments == null || segments.length == 0) {
            timeWithoutSegments = null;
            return;
        }

        boolean foundNonhighlightSegments = false;
        long timeWithoutSegmentsValue = currentVideoLength;

        for (int i = 0, length = segments.length; i < length; i++) {
            SponsorSegment segment = segments[i];
            if (segment.category == SegmentCategory.HIGHLIGHT) {
                continue;
            }
            foundNonhighlightSegments = true;
            long start = segment.start;
            final long end = segment.end;
            // To prevent nested segments from incorrectly counting additional time,
            // check if the segment overlaps any earlier segments.
            for (int j = 0; j < i; j++) {
                start = Math.max(start, segments[j].end);
            }
            if (start < end) {
                timeWithoutSegmentsValue -= (end - start);
            }
        }

        if (!foundNonhighlightSegments) {
            timeWithoutSegments = null;
            return;
        }

        final long hours = timeWithoutSegmentsValue / 3600000;
        final long minutes = (timeWithoutSegmentsValue / 60000) % 60;
        final long seconds = (timeWithoutSegmentsValue / 1000) % 60;
        if (hours > 0) {
            timeWithoutSegments = String.format("\u2009(%d:%02d:%02d)", hours, minutes, seconds);
        } else {
            timeWithoutSegments = String.format("\u2009(%d:%02d)", minutes, seconds);
        }
    }

    private static int highlightSegmentTimeBarScreenWidth = -1; // actual pixel width to use
    private static int getHighlightSegmentTimeBarScreenWidth() {
        if (highlightSegmentTimeBarScreenWidth == -1) {
            highlightSegmentTimeBarScreenWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, HIGHLIGHT_SEGMENT_DRAW_BAR_WIDTH,
                    Objects.requireNonNull(ReVancedUtils.getContext()).getResources().getDisplayMetrics());
        }
        return highlightSegmentTimeBarScreenWidth;
    }

    /**
     * Injection point.
     */
    public static void drawSponsorTimeBars(final Canvas canvas, final float posY) {
        try {
            if (segments == null) return;
            final long videoLength = VideoInformation.getVideoLength();
            if (videoLength <= 0) return;

            final int thicknessDiv2 = sponsorBarThickness / 2; // rounds down
            final float top = posY - (sponsorBarThickness - thicknessDiv2);
            final float bottom = posY + thicknessDiv2;
            final float videoMillisecondsToPixels = (1f / videoLength) * (sponsorAbsoluteBarRight - sponsorBarAbsoluteLeft);
            final float leftPadding = sponsorBarAbsoluteLeft;

            for (SponsorSegment segment : segments) {
                final float left = leftPadding + segment.start * videoMillisecondsToPixels;
                final float right;
                if (segment.category == SegmentCategory.HIGHLIGHT) {
                    right = left + getHighlightSegmentTimeBarScreenWidth();
                } else {
                     right = leftPadding + segment.end * videoMillisecondsToPixels;
                }
                canvas.drawRect(left, top, right, bottom, segment.category.paint);
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "drawSponsorTimeBars failure", ex);
        }
    }

}
