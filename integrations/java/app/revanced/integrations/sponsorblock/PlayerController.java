package app.revanced.integrations.sponsorblock;

import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.timeWithoutSegments;
import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.videoHasSegments;

import android.annotation.SuppressLint;
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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.videoplayer.VideoInformation;
import app.revanced.integrations.whitelist.Whitelist;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.sponsorblock.requests.SBRequester;
import app.revanced.integrations.utils.ReVancedUtils;

@SuppressLint({"LongLogTag"})
public class PlayerController {

    private static final Timer sponsorTimer = new Timer("sponsor-skip-timer");
    public static WeakReference<Activity> playerActivity = new WeakReference<>(null);
    public static SponsorSegment[] sponsorSegmentsOfCurrentVideo;
    private static WeakReference<Object> currentPlayerController = new WeakReference<>(null);
    private static Method setMillisecondMethod;
    private static long allowNextSkipRequestTime = 0L;
    private static String currentVideoId;
    private static long currentVideoLength = 1L;
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

        Context context = ReVancedUtils.getContext();
        SponsorBlockSettings.update(context);

        if (!SettingsEnum.SB_ENABLED.getBoolean()) {
            currentVideoId = null;
            return;
        }

        if (videoId.equals(currentVideoId))
            return;

        currentVideoId = videoId;
        sponsorSegmentsOfCurrentVideo = null;
        LogHelper.debug(PlayerController.class, "setCurrentVideoId: videoId=" + videoId);

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
    public static void onCreate(final Object o) {
        if (SettingsEnum.shorts_playing) return;

//      "Plugin.printStackTrace();

        if (o == null) {
            LogHelper.printException(PlayerController.class, "onCreate called with null object");
            return;
        }

        LogHelper.debug(PlayerController.class, String.format("onCreate called with object %s on thread %s", o.toString(), Thread.currentThread().toString()));

        try {
            setMillisecondMethod = o.getClass().getMethod("replaceMeWithsetMillisecondMethod", Long.TYPE);
            setMillisecondMethod.setAccessible(true);

            lastKnownVideoTime = 0;
            VideoInformation.lastKnownVideoTime = 0;
            currentVideoLength = 1;
            currentPlayerController = new WeakReference<>(o);

            SkipSegmentView.hide();
            NewSegmentHelperLayout.hide();
        } catch (Exception e) {
            LogHelper.printException(PlayerController.class, "Exception while initializing skip method", e);
        }
    }

    public static void executeDownloadSegments(String videoId) {
        videoHasSegments = false;
        timeWithoutSegments = "";
        if (Whitelist.isChannelSBWhitelisted())
            return;
        SponsorSegment[] segments = SBRequester.getSegments(videoId);
        Arrays.sort(segments);

        for (SponsorSegment segment : segments) {
            LogHelper.debug(PlayerController.class, "Detected segment: " + segment.toString());
        }

        sponsorSegmentsOfCurrentVideo = segments;
//        new Handler(Looper.getMainLooper()).post(findAndSkipSegmentRunnable);
    }

    /**
     * Called when it's time to update the UI with new second, about once per second, only when playing, also in background
     */
    public static void setCurrentVideoTime(long millis) {
        if (SettingsEnum.shorts_playing) return;

        LogHelper.debug(PlayerController.class, "setCurrentVideoTime: current video time: " + millis);
        VideoInformation.lastKnownVideoTime = millis;
        if (!SettingsEnum.SB_ENABLED.getBoolean()) return;
        lastKnownVideoTime = millis;
        if (millis <= 0) return;
        //findAndSkipSegment(false);

        if (millis == currentVideoLength) {
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
                    LogHelper.debug(PlayerController.class, "Scheduling skipSponsorTask");
                    skipSponsorTask = new TimerTask() {
                        @Override
                        public void run() {
                            skipSponsorTask = null;
                            lastKnownVideoTime = segment.start + 1;
                            VideoInformation.lastKnownVideoTime = lastKnownVideoTime;
                            new Handler(Looper.getMainLooper()).post(findAndSkipSegmentRunnable);
                        }
                    };
                    sponsorTimer.schedule(skipSponsorTask, segment.start - millis);
                } else {
                    LogHelper.debug(PlayerController.class, "skipSponsorTask is already scheduled...");
                }

                break;
            }

            if (segment.end < millis)
                continue;

            // we are in the segment!
            if (segment.category.behaviour.skip) {
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

    /**
     * Called very high frequency (once every about 100ms), also in background. It sometimes triggers when a video is paused (couple times in the row with the same value)
     */
    public static void setCurrentVideoTimeHighPrecision(final long millis) {
        if (SettingsEnum.shorts_playing) return;

        if ((millis < lastKnownVideoTime && lastKnownVideoTime >= currentVideoLength) || millis == 0) {
            SponsorBlockUtils.showShieldButton(); // skipping from end to the video will show the buttons again
            SponsorBlockUtils.showVoteButton();
        }
        if (lastKnownVideoTime > 0) {
            lastKnownVideoTime = millis;
            VideoInformation.lastKnownVideoTime = lastKnownVideoTime;
        } else
            setCurrentVideoTime(millis);
    }

    public static long getCurrentVideoLength() {
        return currentVideoLength;
    }

    public static long getLastKnownVideoTime() {
        return lastKnownVideoTime;
    }

    /**
     * Called before onDraw method on time bar object, sets video length in millis
     */
    public static void setVideoLength(final long length) {
        if (SettingsEnum.shorts_playing) return;

        LogHelper.debug(PlayerController.class, "setVideoLength: length=" + length);
        currentVideoLength = length;
    }


    public static void setSponsorBarAbsoluteLeft(final Rect rect) {
        if (SettingsEnum.shorts_playing) return;

        setSponsorBarAbsoluteLeft(rect.left);
    }

    public static void setSponsorBarAbsoluteLeft(final float left) {
        LogHelper.debug(PlayerController.class, String.format("setSponsorBarLeft: left=%.2f", left));

        sponsorBarLeft = left;
    }

    public static void setSponsorBarRect(final Object self) {
        if (SettingsEnum.shorts_playing) return;

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
        if (SettingsEnum.shorts_playing) return;

        setSponsorBarAbsoluteRight(rect.right);
    }

    public static void setSponsorBarAbsoluteRight(final float right) {
        LogHelper.debug(PlayerController.class, String.format("setSponsorBarRight: right=%.2f", right));

        sponsorBarRight = right;
    }

    public static void setSponsorBarThickness(final int thickness) {
        if (SettingsEnum.shorts_playing) return;

        setSponsorBarThickness((float) thickness);
    }

    public static void setSponsorBarThickness(final float thickness) {
//        if (VERBOSE_DRAW_OPTIONS)
//            LogH(PlayerController.class, String.format("setSponsorBarThickness: thickness=%.2f", thickness));

        sponsorBarThickness = thickness;
    }

    public static void onSkipSponsorClicked() {
        LogHelper.debug(PlayerController.class, "Skip segment clicked");
        findAndSkipSegment(true);
    }


    public static void addSkipSponsorView15(final View view) {
        if (SettingsEnum.shorts_playing) return;

        playerActivity = new WeakReference<>((Activity) view.getContext());
        LogHelper.debug(PlayerController.class, "addSkipSponsorView15: view=" + view.toString());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).getChildAt(2);
            Activity context = ((Activity) viewGroup.getContext());
            NewSegmentHelperLayout.context = context;
        }, 500);
    }

    public static void addSkipSponsorView14(final View view) {
        playerActivity = new WeakReference<>((Activity) view.getContext());
        LogHelper.debug(PlayerController.class, "addSkipSponsorView14: view=" + view.toString());
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
        if (SettingsEnum.shorts_playing) return;

        if (sponsorBarThickness < 0.1) return;
        if (sponsorSegmentsOfCurrentVideo == null) return;


        final float thicknessDiv2 = sponsorBarThickness / 2;
        final float top = posY - thicknessDiv2;
        final float bottom = posY + thicknessDiv2;
        final float absoluteLeft = sponsorBarLeft;
        final float absoluteRight = sponsorBarRight;

        final float tmp1 = 1f / (float) currentVideoLength * (absoluteRight - absoluteLeft);
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

    public static void skipToMillisecond(long millisecond) {
        // in 15.x if sponsor clip hits the end, then it crashes the app, because of too many function invocations
        // I put this block so that skip can be made only once per some time
        long now = System.currentTimeMillis();
        if (now < allowNextSkipRequestTime) {
            LogHelper.debug(PlayerController.class, "skipToMillisecond: to fast, slow down, because you'll fail");
            return;
        }
        allowNextSkipRequestTime = now + 100;

        if (setMillisecondMethod == null) {
            LogHelper.printException(PlayerController.class, "setMillisecondMethod is null");
            return;
        }


        final Object currentObj = currentPlayerController.get();
        if (currentObj == null) {
            LogHelper.printException(PlayerController.class, "currentObj is null (might have been collected by GC)");
            return;
        }

        LogHelper.debug(PlayerController.class, String.format("Requesting skip to millis=%d on thread %s", millisecond, Thread.currentThread().toString()));

        final long finalMillisecond = millisecond;
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                LogHelper.debug(PlayerController.class, "Skipping to millis=" + finalMillisecond);
                lastKnownVideoTime = finalMillisecond;
                VideoInformation.lastKnownVideoTime = lastKnownVideoTime;
                setMillisecondMethod.invoke(currentObj, finalMillisecond);
            } catch (Exception e) {
                LogHelper.printException(PlayerController.class, "Cannot skip to millisecond", e);
            }
        });
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
            if (!(segment.category.behaviour.skip || wasClicked))
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
        LogHelper.debug(PlayerController.class, "Skipping segment: " + segment.toString());

        if (SettingsEnum.SB_SHOW_TOAST_WHEN_SKIP.getBoolean() && !wasClicked)
            SkipSegmentView.notifySkipped(segment);

        skipToMillisecond(segment.end + 2);
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
