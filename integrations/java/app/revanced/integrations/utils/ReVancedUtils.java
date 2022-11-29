package app.revanced.integrations.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import app.revanced.integrations.sponsorblock.player.PlayerType;

public class ReVancedUtils {

    private static PlayerType env;
    private static boolean newVideo = false;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private ReVancedUtils() {
    } // utility class

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