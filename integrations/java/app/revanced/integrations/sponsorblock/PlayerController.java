package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.timeWithoutSegments;
import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.videoHasSegments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import app.revanced.integrations.patches.VideoInformation;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class PlayerController {

    private static final Timer sponsorTimer = new Timer("sponsor-skip-timer");
    public static WeakReference<Activity> playerActivity = new WeakReference<>(null);
    public static SponsorSegment[] sponsorSegmentsOfCurrentVideo;
    private static long allowNextSkipRequestTime = 0L;
    private static String currentVideoId;
    private static long lastKnownVideoTime = -1L;
    private static final Runnable findAndSkipSegmentRunnable = () -> {
        findAndSkipSegment(false);
    };
    private static float sponsorBarLeft = 1f;
    private static float sponsorBarRight = 1f;
    private static float sponsorBarThickness = 2f;
    private static TimerTask skipSponsorTask = null;

    public static String getCurrentVideoId() {
        return currentVideoId;
    }

    public static void setCurrentVideoId(final String videoId) {
        if (videoId == null) {
            currentVideoId = null;
            sponsorSegmentsOfCurrentVideo = null;
            return;
        }

        // currently this runs every time a video is loaded (regardless if sponsorblock is turned on or off)
        // FIXME: change this so if sponsorblock is disabled, then run this method exactly once and once only
        SponsorBlockSettings.update(null);

        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            currentVideoId = null;
            return;
        }
        if (PlayerType.getCurrent() == PlayerType.NONE) {
            LogHelper.printDebug(() -> "ignoring shorts video");
            currentVideoId = null;
            return;
        }
        if (videoId.equals(currentVideoId))
            return;

        currentVideoId = videoId;
        sponsorSegmentsOfCurrentVideo = null;
        LogHelper.printDebug(() -> "setCurrentVideoId: videoId=" + videoId);

        sponsorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                executeDownloadSegments(currentVideoId);
            }
        }, 0);
    }

    /**
     * Called when creating some kind of youtube internal player controlled, every time when new video starts to play
     */
    public static void initialize(Object _o) {
        lastKnownVideoTime = 0;
        SkipSegmentView.hide();
        NewSegmentHelperLayout.hide();
    }

    public static void executeDownloadSegments(String videoId) {
        videoHasSegments = false;
        timeWithoutSegments = "";

        SponsorSegment[] segments = SBRequester.getSegments(videoId);
        Arrays.sort(segments);

        for (SponsorSegment segment : segments) {
            LogHelper.printDebug(() -> "Detected segment: " + segment.toString());
        }

        sponsorSegmentsOfCurrentVideo = segments;
        // new Handler(Looper.getMainLooper()).post(findAndSkipSegmentRunnable);
    }


    public static void setVideoTime(long millis) {
        LogHelper.printDebug(() -> "setCurrentVideoTime: current video time: " + millis);
        if (!SettingsEnum.SB_ENABLED.getBoolean()) return;
        lastKnownVideoTime = millis;
        if (millis <= 0) return;
        //findAndSkipSegment(false);

        if (millis == VideoInformation.getCurrentVideoLength()) {
            SponsorBlockUtils.hideShieldButton();
            SponsorBlockUtils.hideVoteButton();
            return;
        }

        SponsorSegment[] segments = sponsorSegmentsOfCurrentVideo;
        if (segments == null || segments.length == 0) return;

        final long START_TIMER_BEFORE_SEGMENT_MILLIS = 1200;
        final long startTimerAtMillis = millis + START_TIMER_BEFORE_SEGMENT_MILLIS;

        for (final SponsorSegment segment : segments) {
            if (segment.start > millis) {
                if (segment.start > startTimerAtMillis)
                    break; // it's more then START_TIMER_BEFORE_SEGMENT_MILLIS far away
                if (!segment.category.behaviour.skip)
                    break;

                if (skipSponsorTask == null) {
                    LogHelper.printDebug(() -> "Scheduling skipSponsorTask");
                    skipSponsorTask = new TimerTask() {
                        @Override
                        public void run() {
                            skipSponsorTask = null;
                            lastKnownVideoTime = segment.start + 1;
                            new Handler(Looper.getMainLooper()).post(findAndSkipSegmentRunnable);
                        }
                    };
                    sponsorTimer.schedule(skipSponsorTask, segment.start - millis);
                } else {
                    LogHelper.printDebug(() -> "skipSponsorTask is already scheduled...");
                }

                break;
            }

            if (segment.end < millis)
                continue;

            // we are in the segment!
            if (segment.category.behaviour.skip && !(segment.category.behaviour.key.equals("skip-once") && segment.didAutoSkipped)) {
                sendViewRequestAsync(millis, segment);
                skipSegment(segment, false);
                break;
            } else {
                SkipSegmentView.show();
                return;
            }
        }
        SkipSegmentView.hide();
    }

    private static void sendViewRequestAsync(final long millis, final SponsorSegment segment) {
        if (segment.category != SponsorBlockSettings.SegmentInfo.UNSUBMITTED) {
            Context context = ReVancedUtils.getContext();
            if (context != null) {
                long newSkippedTime = SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.getLong() + (segment.end - segment.start);
                SettingsEnum.SB_SKIPPED_SEGMENTS.saveValue(SettingsEnum.SB_SKIPPED_SEGMENTS.getInt() + 1);
                SettingsEnum.SB_SKIPPED_SEGMENTS_TIME.saveValue(newSkippedTime);
            }
        }
        new Thread(() -> {
            if (SettingsEnum.SB_COUNT_SKIPS.getBoolean() &&
                    segment.category != SponsorBlockSettings.SegmentInfo.UNSUBMITTED &&
                    millis - segment.start < 2000) {
                // Only skips from the start should count as a view
                SBRequester.sendViewCountRequest(segment);
            }
        }).start();
    }

    public static void setHighPrecisionVideoTime(final long millis) {
        if ((millis < lastKnownVideoTime && lastKnownVideoTime >= VideoInformation.getCurrentVideoLength()) || millis == 0) {
            SponsorBlockUtils.showShieldButton(); // skipping from end to the video will show the buttons again
            SponsorBlockUtils.showVoteButton();
        }
        if (lastKnownVideoTime > 0) {
            lastKnownVideoTime = millis;
        } else
            setVideoTime(millis);
    }

    public static long getCurrentVideoLength() {
        return VideoInformation.getCurrentVideoLength();
    }

    public static long getLastKnownVideoTime() {
        return lastKnownVideoTime;
    }

    public static void setSponsorBarAbsoluteLeft(final Rect rect) {
        setSponsorBarAbsoluteLeft(rect.left);
    }

    public static void setSponsorBarAbsoluteLeft(final float left) {
        LogHelper.printDebug(() -> String.format("setSponsorBarLeft: left=%.2f", left));

        sponsorBarLeft = left;
    }

    public static void setSponsorBarRect(final Object self) {
        try {
            Field field = self.getClass().getDeclaredField("replaceMeWithsetSponsorBarRect");
            field.setAccessible(true);
            Rect rect = (Rect) field.get(self);
            if (rect != null) {
                setSponsorBarAbsoluteLeft(rect.left);
                setSponsorBarAbsoluteRight(rect.right);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void setSponsorBarAbsoluteRight(final Rect rect) {
        setSponsorBarAbsoluteRight(rect.right);
    }

    public static void setSponsorBarAbsoluteRight(final float right) {
        LogHelper.printDebug(() -> String.format("setSponsorBarRight: right=%.2f", right));

        sponsorBarRight = right;
    }

    public static void setSponsorBarThickness(final int thickness) {
        setSponsorBarThickness((float) thickness);
    }

    public static void setSponsorBarThickness(final float thickness) {
//        if (VERBOSE_DRAW_OPTIONS)
//            LogH(PlayerController.class, String.format("setSponsorBarThickness: thickness=%.2f", thickness));

        sponsorBarThickness = thickness;
    }

    public static void onSkipSponsorClicked() {
        LogHelper.printDebug(() -> "Skip segment clicked");
        findAndSkipSegment(true);
    }


    public static void addSkipSponsorView15(final View view) {
        playerActivity = new WeakReference<>((Activity) view.getContext());
        LogHelper.printDebug(() -> "addSkipSponsorView15: view=" + view.toString());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(2);
            Activity context = ((Activity) viewGroup.getContext());
            NewSegmentHelperLayout.context = context;
        }, 500);
    }

    public static void addSkipSponsorView14(final View view) {
        playerActivity = new WeakReference<>((Activity) view.getContext());
        LogHelper.printDebug(() -> "addSkipSponsorView14: view=" + view.toString());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            final ViewGroup viewGroup = (ViewGroup) view.getParent();
            Activity activity = (Activity) viewGroup.getContext();
            NewSegmentHelperLayout.context = activity;
        }, 500);
    }


    /**
     * Called when it's time to draw time bar
     */
    public static void drawSponsorTimeBars(final Canvas canvas, final float posY) {
        if (sponsorBarThickness < 0.1) return;
        if (sponsorSegmentsOfCurrentVideo == null) return;


        final float thicknessDiv2 = sponsorBarThickness / 2;
        final float top = posY - thicknessDiv2;
        final float bottom = posY + thicknessDiv2;
        final float absoluteLeft = sponsorBarLeft;
        final float absoluteRight = sponsorBarRight;

        final float tmp1 = 1f / (float) VideoInformation.getCurrentVideoLength() * (absoluteRight - absoluteLeft);
        for (SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
            float left = segment.start * tmp1 + absoluteLeft;
            float right = segment.end * tmp1 + absoluteLeft;
            canvas.drawRect(left, top, right, bottom, segment.category.paint);
        }
    }

    //    private final static Pattern videoIdRegex = Pattern.compile(".*\\.be\\/([A-Za-z0-9_\\-]{0,50}).*");
    public static String substringVideoIdFromLink(String link) {
        return link.substring(link.lastIndexOf('/') + 1);
    }

    public static void skipRelativeMilliseconds(int millisRelative) {
        skipToMillisecond(lastKnownVideoTime + millisRelative);
    }

    public static boolean skipToMillisecond(long millisecond) {
        // in 15.x if sponsor clip hits the end, then it crashes the app, because of too many function invocations
        // I put this block so that skip can be made only once per some time
        long now = System.currentTimeMillis();
        if (now < allowNextSkipRequestTime) {
            LogHelper.printDebug(() -> "skipToMillisecond: to fast, slow down, because you'll fail");
            return false;
        }
        allowNextSkipRequestTime = now + 100;

        LogHelper.printDebug(() -> String.format("Requesting skip to millis=%d on thread %s", millisecond, Thread.currentThread().toString()));

        final long finalMillisecond = millisecond;

        try {
            LogHelper.printDebug(() -> "Skipping to millis=" + finalMillisecond);
            lastKnownVideoTime = finalMillisecond;
            VideoInformation.seekTo(finalMillisecond);
        } catch (Exception e) {
            LogHelper.printException(() -> ("Cannot skip to millisecond"), e);
        }

        return true;
    }


    private static void findAndSkipSegment(boolean wasClicked) {
        if (sponsorSegmentsOfCurrentVideo == null)
            return;

        final long millis = lastKnownVideoTime;

        for (SponsorSegment segment : sponsorSegmentsOfCurrentVideo) {
            if (segment.start > millis)
                break;

            if (segment.end < millis)
                continue;

            SkipSegmentView.show();
            if (!((segment.category.behaviour.skip && !(segment.category.behaviour.key.equals("skip-once") && segment.didAutoSkipped)) || wasClicked))
                return;

            sendViewRequestAsync(millis, segment);
            skipSegment(segment, wasClicked);
            break;
        }

        SkipSegmentView.hide();
    }

    private static void skipSegment(SponsorSegment segment, boolean wasClicked) {
//        if (lastSkippedSegment == segment) return;
//        lastSkippedSegment = segment;
        LogHelper.printDebug(() -> "Skipping segment: " + segment.toString());

        if (SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean() && !wasClicked)
            SkipSegmentView.notifySkipped(segment);

        boolean didSucceed = skipToMillisecond(segment.end + 2);
        if (didSucceed && !wasClicked) {
            segment.didAutoSkipped = true;
        }
        SkipSegmentView.hide();
        if (segment.category == SponsorBlockSettings.SegmentInfo.UNSUBMITTED) {
            SponsorSegment[] newSegments = new SponsorSegment[sponsorSegmentsOfCurrentVideo.length - 1];
            int i = 0;
            for (SponsorSegment sponsorSegment : sponsorSegmentsOfCurrentVideo) {
                if (sponsorSegment != segment)
                    newSegments[i++] = sponsorSegment;
            }
            sponsorSegmentsOfCurrentVideo = newSegments;
        }
    }
}
