package app.revanced.integrations.youtube.patches;

import androidx.annotation.NonNull;
import app.revanced.integrations.youtube.patches.playback.speed.RememberPlaybackSpeedPatch;
import app.revanced.integrations.youtube.shared.VideoState;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Hooking class for the current playing video.
 * @noinspection unused
 */
public final class VideoInformation {

    public interface PlaybackController {
        // Methods are added to YT classes during patching.
        boolean seekTo(long videoTime);
        void seekToRelative(long videoTimeOffset);
    }

    private static final float DEFAULT_YOUTUBE_PLAYBACK_SPEED = 1.0f;
    /**
     * Prefix present in all Short player parameters signature.
     */
    private static final String SHORTS_PLAYER_PARAMETERS = "8AEB";

    private static WeakReference<PlaybackController> playerControllerRef = new WeakReference<>(null);
    private static WeakReference<PlaybackController> mdxPlayerDirectorRef = new WeakReference<>(null);

    @NonNull
    private static String videoId = "";
    private static long videoLength = 0;
    private static long videoTime = -1;

    @NonNull
    private static volatile String playerResponseVideoId = "";
    private static volatile boolean playerResponseVideoIdIsShort;
    private static volatile boolean videoIdIsShort;

    /**
     * The current playback speed
     */
    private static float playbackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;

    /**
     * Injection point.
     *
     * @param playerController player controller object.
     */
    public static void initialize(@NonNull PlaybackController playerController) {
        try {
            playerControllerRef = new WeakReference<>(Objects.requireNonNull(playerController));
            videoTime = -1;
            videoLength = 0;
            playbackSpeed = DEFAULT_YOUTUBE_PLAYBACK_SPEED;
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to initialize", ex);
        }
    }

    /**
     * Injection point.
     *
     * @param mdxPlayerDirector MDX player director object (casting mode).
     */
    public static void initializeMdx(@NonNull PlaybackController mdxPlayerDirector) {
        try {
            mdxPlayerDirectorRef = new WeakReference<>(Objects.requireNonNull(mdxPlayerDirector));
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to initialize MDX", ex);
        }
    }

    /**
     * Injection point.
     *
     * @param newlyLoadedVideoId id of the current video
     */
    public static void setVideoId(@NonNull String newlyLoadedVideoId) {
        if (!videoId.equals(newlyLoadedVideoId)) {
            Logger.printDebug(() -> "New video id: " + newlyLoadedVideoId);
            videoId = newlyLoadedVideoId;
        }
    }

    /**
     * @return If the player parameters are for a Short.
     */
    public static boolean playerParametersAreShort(@NonNull String parameters) {
        return parameters.startsWith(SHORTS_PLAYER_PARAMETERS);
    }

    /**
     * Injection point.
     */
    public static String newPlayerResponseSignature(@NonNull String signature, String videoId, boolean isShortAndOpeningOrPlaying) {
        final boolean isShort = playerParametersAreShort(signature);
        playerResponseVideoIdIsShort = isShort;
        if (!isShort || isShortAndOpeningOrPlaying) {
            if (videoIdIsShort != isShort) {
                videoIdIsShort = isShort;
                Logger.printDebug(() -> "videoIdIsShort: " + isShort);
            }
        }
        return signature; // Return the original value since we are observing and not modifying.
    }

    /**
     * Injection point.  Called off the main thread.
     *
     * @param videoId The id of the last video loaded.
     */
    public static void setPlayerResponseVideoId(@NonNull String videoId, boolean isShortAndOpeningOrPlaying) {
        if (!playerResponseVideoId.equals(videoId)) {
            Logger.printDebug(() -> "New player response video id: " + videoId);
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
        Logger.printDebug(() -> "User selected playback speed: " + userSelectedPlaybackSpeed);
        playbackSpeed = userSelectedPlaybackSpeed;
    }

    /**
     * Overrides the current playback speed.
     * <p>
     * <b> Used exclusively by {@link RememberPlaybackSpeedPatch} </b>
     */
    public static void overridePlaybackSpeed(float speedOverride) {
        if (playbackSpeed != speedOverride) {
            Logger.printDebug(() -> "Overriding playback speed to: " + speedOverride);
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
            Logger.printDebug(() -> "Current video length: " + length);
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
     * @param seekTime The seekTime to seek the video to.
     * @return true if the seek was successful.
     */
    public static boolean seekTo(final long seekTime) {
        Utils.verifyOnMainThread();
        try {
            final long videoTime = getVideoTime();
            final long videoLength = getVideoLength();

            // Prevent issues such as play/ pause button or autoplay not working.
            final long adjustedSeekTime = Math.min(seekTime, videoLength - 250);
            if (videoTime <= seekTime && videoTime >= adjustedSeekTime) {
                // Both the current video time and the seekTo are in the last 250ms of the video.
                // Ignore this seek call, otherwise if a video ends with multiple closely timed segments
                // then seeking here can create an infinite loop of skip attempts.
                Logger.printDebug(() -> "Ignoring seekTo call as video playback is almost finished. "
                        + " videoTime: " + videoTime + " videoLength: " + videoLength + " seekTo: " + seekTime);
                return false;
            }

            Logger.printDebug(() -> "Seeking to: " + adjustedSeekTime);

            // Try regular playback controller first, and it will not succeed if casting.
            PlaybackController controller = playerControllerRef.get();
            if (controller == null) {
                Logger.printDebug(() -> "Cannot seekTo because player controller is null");
            } else {
                if (controller.seekTo(adjustedSeekTime)) return true;
                Logger.printDebug(() -> "seekTo did not succeeded. Trying MXD.");
                // Else the video is loading or changing videos, or video is casting to a different device.
            }

            // Try calling the seekTo method of the MDX player director (called when casting).
            // The difference has to be a different second mark in order to avoid infinite skip loops
            // as the Lounge API only supports seconds.
            if (adjustedSeekTime / 1000 == videoTime / 1000) {
                Logger.printDebug(() -> "Skipping seekTo for MDX because seek time is too small "
                        + "(" + (adjustedSeekTime - videoTime) + "ms)");
                return false;
            }

            controller = mdxPlayerDirectorRef.get();
            if (controller == null) {
                Logger.printDebug(() -> "Cannot seekTo MXD because player controller is null");
                return false;
            }

            return controller.seekTo(adjustedSeekTime);
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to seek", ex);
            return false;
        }
    }

    /**
     * Seeks a relative amount.  Should always be used over {@link #seekTo(long)}
     * when the desired seek time is an offset of the current time.
     */
    public static void seekToRelative(long seekTime) {
        Utils.verifyOnMainThread();
        try {
            Logger.printDebug(() -> "Seeking relative to: " + seekTime);

            // 19.39+ does not have a boolean return type for relative seek.
            // But can call both methods and it works correctly for both situations.
            PlaybackController controller = playerControllerRef.get();
            if (controller == null) {
                Logger.printDebug(() -> "Cannot seek relative as player controller is null");
            } else {
                controller.seekToRelative(seekTime);
            }

            // Adjust the fine adjustment function so it's at least 1 second before/after.
            // Otherwise the fine adjustment will do nothing when casting.
            final long adjustedSeekTime;
            if (seekTime < 0) {
                adjustedSeekTime = Math.min(seekTime, -1000);
            } else {
                adjustedSeekTime = Math.max(seekTime, 1000);
            }

            controller = mdxPlayerDirectorRef.get();
            if (controller == null) {
                Logger.printDebug(() -> "Cannot seek relative as MXD player controller is null");
            } else {
                controller.seekToRelative(adjustedSeekTime);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to seek relative", ex);
        }
    }

    /**
     * Id of the last video opened.  Includes Shorts.
     *
     * @return The id of the video, or an empty string if no videos have been opened yet.
     */
    @NonNull
    public static String getVideoId() {
        return videoId;
    }

    /**
     * Differs from {@link #videoId} as this is the video id for the
     * last player response received, which may not be the last video opened.
     * <p>
     * If Shorts are loading the background, this commonly will be
     * different from the Short that is currently on screen.
     * <p>
     * For most use cases, you should instead use {@link #getVideoId()}.
     *
     * @return The id of the last video loaded, or an empty string if no videos have been loaded yet.
     */
    @NonNull
    public static String getPlayerResponseVideoId() {
        return playerResponseVideoId;
    }

    /**
     * @return If the last player response video id was a Short.
     * Includes Shorts shelf items appearing in the feed that are not opened.
     * @see #lastVideoIdIsShort()
     */
    public static boolean lastPlayerResponseIsShort() {
        return playerResponseVideoIdIsShort;
    }

    /**
     * @return If the last player response video id _that was opened_ was a Short.
     */
    public static boolean lastVideoIdIsShort() {
        return videoIdIsShort;
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isAtEndOfVideo() {
        return videoTime >= videoLength && videoLength > 0;
    }

}
