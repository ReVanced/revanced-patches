package app.revanced.integrations.patches;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import app.revanced.integrations.utils.LogHelper;

/**
 * Hooking class for the player controller.
 */
public final class PlayerControllerPatch {
    private static final String SEEK_METHOD_NAME = "seekTo";

    private static WeakReference<Object> playerController;
    private static Method seekMethod;
    private static long videoLength = 1;

    /**
     * Hook into PlayerController.onCreate() method.
     *
     * @param thisRef Reference to the player controller object.
     */
    public static void playerController_onCreateHook(final Object thisRef) {
        playerController = new WeakReference<>(thisRef);
        videoLength = 1;

        try {
            seekMethod = thisRef.getClass().getMethod(SEEK_METHOD_NAME, Long.TYPE);
            seekMethod.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            LogHelper.debug(PlayerControllerPatch.class, "Failed to initialize: " + ex.getMessage());
        }
    }

    /**
     * Set the current video length.
     *
     * @param length The length of the video in milliseconds.
     */
    public static void setCurrentVideoLength(final long length) {
        LogHelper.debug(PlayerControllerPatch.class, "Setting current video length to " + length);

        videoLength = length;
    }

    /**
     * Seek on the current video.
     *
     * @param millisecond The millisecond to seek the video to.
     */
    public static void seekTo(final long millisecond) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (seekMethod == null) {
                LogHelper.debug(PlayerControllerPatch.class, "seekMethod was null");
                return;
            }

            try {
                LogHelper.debug(PlayerControllerPatch.class, "Seeking to " + millisecond);
                seekMethod.invoke(playerController.get(), millisecond);
            } catch (Exception ex) {
                LogHelper.debug(PlayerControllerPatch.class, "Failed to seek: " + ex.getMessage());
            }
        });
    }

    /**
     * Get the length of the current video playing.
     *
     * @return The length of the video in milliseconds.
     */
    public static long getCurrentVideoLength() {
        return videoLength;
    }
}
