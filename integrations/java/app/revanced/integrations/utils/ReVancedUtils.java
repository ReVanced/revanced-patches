package app.revanced.integrations.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import app.revanced.integrations.sponsorblock.player.PlayerType;

public class ReVancedUtils {

    private static PlayerType env;
    private static boolean newVideo = false;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private ReVancedUtils() {
    } // utility class

    /**
     * Maximum number of background threads run concurrently
     */
    private static final int SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS = 20;

    /**
     * General purpose pool for network calls and other background tasks.
     */
    private static final ThreadPoolExecutor backgroundThreadPool = new ThreadPoolExecutor(
            2, // minimum 2 threads always ready to be used
            10, // For any threads over the minimum, keep them alive 10 seconds after they go idle
            SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private static void checkIfPoolHasReachedLimit() {
        if (backgroundThreadPool.getActiveCount() >= SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS) {
            // Something is wrong. Background threads are piling up and not completing as expected,
            // or some ReVanced code is submitting an unexpected number of background tasks.
            LogHelper.printException(() -> "Reached maximum background thread count of "
                    + SHARED_THREAD_POOL_MAXIMUM_BACKGROUND_THREADS + " threads");

            // Because this condition will manifest as a slow running app or a memory leak,
            // it might be best to show the user a toast or some other suggestion to restart the app.
            // TODO? if debug is enabled, show a toast?
        }
    }

    public static void runOnBackgroundThread(Runnable task) {
        checkIfPoolHasReachedLimit();
        backgroundThreadPool.execute(task);
    }

    public static <T> Future<T> submitOnBackgroundThread(Callable<T> call) {
        checkIfPoolHasReachedLimit();
        return backgroundThreadPool.submit(call);
    }

    public static boolean containsAny(final String value, final String... targets) {
        for (String string : targets)
            if (!string.isEmpty() && value.contains(string)) return true;
        return false;
    }

    public static void setNewVideo(boolean started) {
        LogHelper.printDebug(() -> "New video started: " + started);
        newVideo = started;
    }

    public static boolean isNewVideoStarted() {
        return newVideo;
    }

    public static Integer getResourceIdByName(Context context, String type, String name) {
        try {
            Resources res = context.getResources();
            return res.getIdentifier(name, type, context.getPackageName());
        } catch (Throwable exception) {
            LogHelper.printException(() -> ("Resource not found."), exception);
            return null;
        }
    }

    public static void setPlayerType(PlayerType type) {
        env = type;
    }

    public static PlayerType getPlayerType() {
        return env;
    }

    public static int getIdentifier(String name, String defType) {
        Context context = getContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    public static Context getContext() {
        if (context != null) {
            return context;
        } else {
            LogHelper.printException(() -> ("Context is null, returning null!"));
            return null;
        }
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * @return if the calling thread is on the main thread
     */
    public static boolean currentIsOnMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }

    /**
     * @throws IllegalStateException if the calling thread is _not_ on the main thread
     */
    public static void verifyOnMainThread() throws IllegalStateException {
        if (!currentIsOnMainThread()) {
            throw new IllegalStateException("Must call _on_ the main thread");
        }
    }

    /**
     * @throws IllegalStateException if the calling thread _is_ on the main thread
     */
    public static void verifyOffMainThread() throws IllegalStateException {
        if (currentIsOnMainThread()) {
            throw new IllegalStateException("Must call _off_ the main thread");
        }
    }
}