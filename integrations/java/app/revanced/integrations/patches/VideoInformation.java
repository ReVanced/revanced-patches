package app.revanced.integrations.patches;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import app.revanced.integrations.patches.playback.speed.RememberPlaybackSpeedPatch;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Hooking class for the current playing video.
 */
public final class VideoInformation {
    private static final float DEFAULT_YOUTUBE_PLAYBACK_SPEED = 1.0f;
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerController;
    private static Method seekMethod;

    @NonNull
    private static String videoId = "";
    private static long videoLength = 0;
    private static volatile long videoTime = -1; // must be volatile. Value is set off main thread from high precision patch hook
    /**
     * The current playback speed
     */
    private static float playbackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;

    /**
     * Injection point.
     * Sets a reference to the YouTube playback controller.
     *
     * @param thisRef Reference to the player controller object.
     */
    public static void playerController_onCreateHook(final Object thisRef) {
        playerController = new WeakReference<>(thisRef);
        videoLength = 0;
        videoTime = -1;

        try {
            seekMethod = thisRef.getClass().getMethod(SEEK_METHOD_NAME, Long.TYPE);
            seekMethod.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            LogHelper.printException(() -> "Failed to initialize", ex);
        }
    }

    /**
     * Injection point.
     *
     * @param newlyLoadedVideoId id of the current video
     */
    public static void setVideoId(@NonNull String newlyLoadedVideoId) {
        if (!videoId.equals(newlyLoadedVideoId)) {
            LogHelper.printDebug(() -> "New video id: " + newlyLoadedVideoId);
            videoId = newlyLoadedVideoId;
            playbackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;
        }
    }

    /**
     * Injection point.
     * Called when user selects a playback speed.
     *
     * @param userSelectedPlaybackSpeed The playback speed the user selected
     */
    public static void userSelectedPlaybackSpeed(float userSelectedPlaybackSpeed) {
        LogHelper.printDebug(() -> "User selected playback speed: " + userSelectedPlaybackSpeed);
        playbackSpeed = userSelectedPlaybackSpeed;
    }

    /**
     * Overrides the current playback speed.
     *
     * <b> Used exclusively by {@link RememberPlaybackSpeedPatch} </b>
     */
    public static void overridePlaybackSpeed(float speedOverride) {
        if (playbackSpeed != speedOverride) {
            LogHelper.printDebug(() -> "Overriding playback speed to: " + speedOverride);
            playbackSpeed = speedOverride;
        }
    }

    /**
     * Injection point.
     *
     * @param length The length of the video in milliseconds.
     */
    public static void setVideoLength(final long length) {
        if (videoLength != length) {
            LogHelper.printDebug(() -> "Current video length: " + length);
            videoLength = length;
        }
    }

    /**
     * Injection point.
     * Called off the main thread approximately every 50ms to 100ms
     *
     * @param currentPlaybackTime The current playback time of the video in milliseconds.
     */
    public static void setVideoTimeHighPrecision(final long currentPlaybackTime) {
        videoTime = currentPlaybackTime;
    }

    /**
     * Seek on the current video.
     * Does not function for playback of Shorts or Stories.
     *
     * Caution: If called from a videoTimeHook() callback,
     * this will cause a recursive call into the same videoTimeHook() callback.
     *
     * @param millisecond The millisecond to seek the video to.
     * @return if the seek was successful
     */
    public static boolean seekTo(final long millisecond) {
        ReVancedUtils.verifyOnMainThread();
        if (seekMethod == null) {
            LogHelper.printException(() -> "seekMethod was null");
            return false;
        }

        try {
            LogHelper.printDebug(() -> "Seeking to " + millisecond);
            return (Boolean) seekMethod.invoke(playerController.get(), millisecond);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to seek", ex);
            return false;
        }
    }

    public static boolean seekToRelative(long millisecondsRelative) {
        return seekTo(videoTime + millisecondsRelative);
    }

    /**
     * Id of the current video playing.  Includes Shorts and YouTube Stories.
     *
     * @return The id of the video. Empty string if not set yet.
     */
    @NonNull
    public static String getCurrentVideoId() {
        return videoId;
    }

    /**
     * @return The current playback speed.
     */
    public static float getCurrentPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Length of the current video playing.
     * Includes Shorts playback.
     *
     * @return The length of the video in milliseconds.
     *         If the video is not yet loaded, or if the video is playing in the background with no video visible,
     *         then this returns zero.
     */
    public static long getCurrentVideoLength() {
       return videoLength;
    }

    /**
     * Playback time of the current video playing.
     * Value can lag up to approximately 100ms behind the actual current video playback time.
     *
     * Note: Code inside a videoTimeHook patch callback
     * should use the callback video time and avoid using this method
     * (in situations of recursive hook callbacks, the value returned here may be outdated).
     *
     * Includes Shorts playback.
     *
     * @return The time of the video in milliseconds. -1 if not set yet.
     */
    public static long getVideoTime() {
        return videoTime;
    }

    /**
     * @return If the playback is at the end of the video.
     *
     * If video is playing in the background with no video visible,
     * this always returns false (even if the video is actually at the end)
     */
    public static boolean isAtEndOfVideo() {
        return videoTime > 0 && videoLength > 0 && videoTime >= videoLength;
    }

}
