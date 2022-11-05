package app.revanced.integrations.patches;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import app.revanced.integrations.utils.LogHelper;

/**
 * Hooking class for the current playing video.
 */
public final class VideoInformation {
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerController;
    private static Method seekMethod;

    private static long videoLength = 1;
    private static long videoTime = -1;


    /**
     * Hook into PlayerController.onCreate() method.
     *
     * @param thisRef Reference to the player controller object.
     */
    public static void playerController_onCreateHook(final Object thisRef) {
        playerController = new WeakReference<>(thisRef);
        videoLength = 1;
        videoTime = -1;

        try {
            seekMethod = thisRef.getClass().getMethod(SEEK_METHOD_NAME, Long.TYPE);
            seekMethod.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            LogHelper.debug(VideoInformation.class, "Failed to initialize: " + ex.getMessage());
        }
    }

    /**
     * Set the video length.
     *
     * @param length The length of the video in milliseconds.
     */
    public static void setVideoLength(final long length) {
        LogHelper.debug(VideoInformation.class, "Setting current video length to " + length);
        videoLength = length;
    }

    /**
     * Set the video time.
     *
     * @param time The time of the video in milliseconds.
     */
    public static void setVideoTime(final long time) {
        LogHelper.debug(VideoInformation.class, "Current video time " + time);
        videoTime = time;
    }

    /**
     * Seek on the current video.
     *
     * @param millisecond The millisecond to seek the video to.
     */
    public static void seekTo(final long millisecond) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (seekMethod == null) {
                LogHelper.debug(VideoInformation.class, "seekMethod was null");
                return;
            }

            try {
                LogHelper.debug(VideoInformation.class, "Seeking to " + millisecond);
                seekMethod.invoke(playerController.get(), millisecond);
            } catch (Exception ex) {
                LogHelper.debug(VideoInformation.class, "Failed to seek: " + ex.getMessage());
            }
        });
    }

    /**
     * Get the length of the current video playing.
     *
     * @return The length of the video in milliseconds. 1 if not set yet.
     */
    public static long getCurrentVideoLength() {
       return videoLength;
    }

    /**
     * Get the time of the current video playing.
     *
     * @return The time of the video in milliseconds. -1 if not set yet.
     */
    public static long getVideoTime() {
        return videoTime;
    }
}
