package app.revanced.integrations.patches;

import androidx.annotation.NonNull;
import app.revanced.integrations.patches.playback.speed.RememberPlaybackSpeedPatch;
import app.revanced.integrations.shared.VideoState;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Hooking class for the current playing video.
 * @noinspection unused
 */
public final class VideoInformation {
    private static final float DEFAULT_YOUTUBE_PLAYBACK_SPEED = 1.0f;
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerControllerRef;
    private static Method seekMethod;

    @NonNull
    private static String videoId = "";
    private static long videoLength = 0;
    private static long videoTime = -1;

    @NonNull
    private static volatile String playerResponseVideoId = "";

    /**
     * The current playback speed
     */
    private static float playbackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;

    /**
     * Injection point.
     *
     * @param playerController player controller object.
     */
    public static void initialize(@NonNull Object playerController) {
        try {
            playerControllerRef = new WeakReference<>(Objects.requireNonNull(playerController));
            videoTime = -1;
            videoLength = 0;
            playbackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;

            seekMethod = playerController.getClass().getMethod(SEEK_METHOD_NAME, Long.TYPE);
            seekMethod.setAccessible(true);
        } catch (Exception ex) {
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
        }
    }

    /**
     * Injection point.  Called off the main thread.
     *
     * @param videoId The id of the last video loaded.
     */
    public static void setPlayerResponseVideoId(@NonNull String videoId, boolean videoIsOpeningOrPlaying) {
        if (!playerResponseVideoId.equals(videoId)) {
            LogHelper.printDebug(() -> "New player response video id: " + videoId);
            playerResponseVideoId = videoId;
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
     * <p>
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
     * Called on the main thread every 1000ms.
     *
     * @param currentPlaybackTime The current playback time of the video in milliseconds.
     */
    public static void setVideoTime(final long currentPlaybackTime) {
        videoTime = currentPlaybackTime;
    }

    /**
     * Seek on the current video.
     * Does not function for playback of Shorts.
     * <p>
     * Caution: If called from a videoTimeHook() callback,
     * this will cause a recursive call into the same videoTimeHook() callback.
     *
     * @param millisecond The millisecond to seek the video to.
     * @return true if the seek was successful.
     */
    public static boolean seekTo(final long millisecond) {
        final long videoLength = getVideoLength();

        // Prevent issues such as play/ pause button or autoplay not working.
        final long seekToMilliseconds = Math.min(millisecond, VideoInformation.getVideoLength() - 250);

        ReVancedUtils.verifyOnMainThread();
        try {
            LogHelper.printDebug(() -> "Seeking to " + seekToMilliseconds);
            //noinspection DataFlowIssue
            return (Boolean) seekMethod.invoke(playerControllerRef.get(), seekToMilliseconds);
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to seek", ex);
            return false;
        }
    }

    /** @noinspection UnusedReturnValue*/
    public static boolean seekToRelative(long millisecondsRelative) {
        return seekTo(videoTime + millisecondsRelative);
    }

    /**
     * Id of the current video playing.  Includes Shorts.
     *
     * @return The id of the video. Empty string if not set yet.
     */
    @NonNull
    public static String getVideoId() {
        return videoId;
    }

    /**
     * Differs from {@link #videoId} as this is the video id for the
     * last player response received, which may not be the current video playing.
     * <p>
     * If Shorts are loading the background, this commonly will be
     * different from the Short that is currently on screen.
     * <p>
     * For most use cases, you should instead use {@link #getVideoId()}.
     *
     * @return The id of the last video loaded. Empty string if not set yet.
     */
    @NonNull
    public static String getPlayerResponseVideoId() {
        return playerResponseVideoId;
    }

    /**
     * @return The current playback speed.
     */
    public static float getPlaybackSpeed() {
        return playbackSpeed;
    }

    /**
     * Length of the current video playing.  Includes Shorts.
     *
     * @return The length of the video in milliseconds.
     *         If the video is not yet loaded, or if the video is playing in the background with no video visible,
     *         then this returns zero.
     */
    public static long getVideoLength() {
       return videoLength;
    }

    /**
     * Playback time of the current video playing.  Includes Shorts.
     * <p>
     * Value will lag behind the actual playback time by a variable amount based on the playback speed.
     * <p>
     * If playback speed is 2.0x, this value may be up to 2000ms behind the actual playback time.
     * If playback speed is 1.0x, this value may be up to 1000ms behind the actual playback time.
     * If playback speed is 0.5x, this value may be up to 500ms behind the actual playback time.
     * Etc.
     *
     * @return The time of the video in milliseconds. -1 if not set yet.
     */
    public static long getVideoTime() {
        return videoTime;
    }

    /**
     * @return If the playback is at the end of the video.
     *        <p>
     * If video is playing in the background with no video visible,
     * this always returns false (even if the video is actually at the end).
     * <p>
     * This is equivalent to checking for {@link VideoState#ENDED},
     * but can give a more up-to-date result for code calling from some hooks.
     *
     * @see VideoState
     */
    public static boolean isAtEndOfVideo() {
        return videoTime >= videoLength && videoLength > 0;
    }

}
