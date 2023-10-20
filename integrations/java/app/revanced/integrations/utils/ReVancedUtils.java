package app.revanced.integrations.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.integrations.settings.SettingsEnum;

import java.text.Bidi;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;

public class ReVancedUtils {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private static String versionName;

    private ReVancedUtils() {
    } // utility class

    public static String getVersionName() {
        if (versionName != null) return versionName;

        PackageInfo packageInfo;
        try {
            final var packageName = Objects.requireNonNull(getContext()).getPackageName();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                packageInfo = context.getPackageManager().getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0)
                );
            else
                packageInfo = context.getPackageManager().getPackageInfo(
                        packageName,
                        0
                );
        } catch (PackageManager.NameNotFoundException e) {
            LogHelper.printException(() -> "Failed to get package info", e);
            return null;
        }

        return versionName = packageInfo.versionName;
    }

    /**
     * Hide a view by setting its layout height and width to 1dp.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    public static void hideViewBy1dpUnderCondition(SettingsEnum condition, View view) {
        if (!condition.getBoolean()) return;

        LogHelper.printDebug(() -> "Hiding view with setting: " + condition);

        hideViewByLayoutParams(view);
    }

    /**
     * Hide a view by setting its visibility to GONE.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    public static void hideViewUnderCondition(SettingsEnum condition, View view) {
        if (!condition.getBoolean()) return;

        LogHelper.printDebug(() -> "Hiding view with setting: " + condition);

        view.setVisibility(View.GONE);
    }


    /**
     * General purpose pool for network calls and other background tasks.
     * All tasks run at max thread priority.
     */
    private static final ThreadPoolExecutor backgroundThreadPool = new ThreadPoolExecutor(
            2, // 2 threads always ready to go
            Integer.MAX_VALUE,
            10, // For any threads over the minimum, keep them alive 10 seconds after they go idle
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> { // ThreadFactory
                Thread t = new Thread(r);
                t.setPriority(Thread.MAX_PRIORITY); // run at max priority
                return t;
            });

    public static void runOnBackgroundThread(@NonNull Runnable task) {
        backgroundThreadPool.execute(task);
    }

    @NonNull
    public static <T> Future<T> submitOnBackgroundThread(@NonNull Callable<T> call) {
        return backgroundThreadPool.submit(call);
    }

    public static boolean containsAny(@NonNull String value, @NonNull String... targets) {
        return indexOfFirstFound(value, targets) >= 0;
    }

    public static int indexOfFirstFound(@NonNull String value, @NonNull String... targets) {
        for (String string : targets) {
            if (!string.isEmpty()) {
                final int indexOf = value.indexOf(string);
                if (indexOf >= 0) return indexOf;
            }
        }
        return -1;
    }

    /**
     * @return zero, if the resource is not found
     */
    @SuppressLint("DiscouragedApi")
    public static int getResourceIdentifier(@NonNull Context context, @NonNull String resourceIdentifierName, @NonNull String type) {
        return context.getResources().getIdentifier(resourceIdentifierName, type, context.getPackageName());
    }

    /**
     * @return zero, if the resource is not found
     */
    public static int getResourceIdentifier(@NonNull String resourceIdentifierName, @NonNull String type) {
        return getResourceIdentifier(getContext(), resourceIdentifierName, type);
    }

    public static int getResourceInteger(@NonNull String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getInteger(getResourceIdentifier(resourceIdentifierName, "integer"));
    }

    @NonNull
    public static Animation getResourceAnimation(@NonNull String resourceIdentifierName) throws Resources.NotFoundException {
        return AnimationUtils.loadAnimation(getContext(), getResourceIdentifier(resourceIdentifierName, "anim"));
    }

    public static int getResourceColor(@NonNull String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getColor(getResourceIdentifier(resourceIdentifierName, "color"));
    }

    public static int getResourceDimensionPixelSize(@NonNull String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getDimensionPixelSize(getResourceIdentifier(resourceIdentifierName, "dimen"));
    }

    public static float getResourceDimension(@NonNull String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getDimension(getResourceIdentifier(resourceIdentifierName, "dimen"));
    }

    /**
     * @return The first child view that matches the filter.
     */
    @Nullable
    public static <T extends View> T getChildView(@NonNull ViewGroup viewGroup, @NonNull MatchFilter filter) {
        for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (filter.matches(childAt)) {
                return (T) childAt;
            }
        }
        return null;
    }

    public interface MatchFilter<T> {
        boolean matches(T object);
    }

    public static Context getContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException(() -> "Context is null, returning null!");
        return null;
    }

    public static void setClipboard(@NonNull String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("ReVanced", text);
        clipboard.setPrimaryClip(clip);
    }

    public static boolean isTablet() {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    @Nullable
    private static Boolean isRightToLeftTextLayout;

    /**
     * If the device language uses right to left text layout (hebrew, arabic, etc)
     */
    public static boolean isRightToLeftTextLayout() {
        if (isRightToLeftTextLayout == null) {
            String displayLanguage = Locale.getDefault().getDisplayLanguage();
            isRightToLeftTextLayout = new Bidi(displayLanguage, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft();
        }
        return isRightToLeftTextLayout;
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastShort(@NonNull String messageToToast) {
        showToast(messageToToast, Toast.LENGTH_SHORT);
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastLong(@NonNull String messageToToast) {
        showToast(messageToToast, Toast.LENGTH_LONG);
    }

    private static void showToast(@NonNull String messageToToast, int toastDuration) {
        Objects.requireNonNull(messageToToast);
        runOnMainThreadNowOrLater(() -> {
                    // cannot use getContext(), otherwise if context is null it will cause infinite recursion of error logging
                    if (context == null) {
                        LogHelper.printDebug(() -> "Cannot show toast (context is null)");
                    } else {
                        LogHelper.printDebug(() -> "Showing toast: " + messageToToast);
                        Toast.makeText(context, messageToToast, toastDuration).show();
                    }
                }
        );
    }

    /**
     * Automatically logs any exceptions the runnable throws.
     *
     * @see #runOnMainThreadNowOrLater(Runnable)
     */
    public static void runOnMainThread(@NonNull Runnable runnable) {
        runOnMainThreadDelayed(runnable, 0);
    }

    /**
     * Automatically logs any exceptions the runnable throws
     */
    public static void runOnMainThreadDelayed(@NonNull Runnable runnable, long delayMillis) {
        Runnable loggingRunnable = () -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                LogHelper.printException(() -> runnable.getClass() + ": " + ex.getMessage(), ex);
            }
        };
        new Handler(Looper.getMainLooper()).postDelayed(loggingRunnable, delayMillis);
    }

    /**
     * If called from the main thread, the code is run immediately.<p>
     * If called off the main thread, this is the same as {@link #runOnMainThread(Runnable)}.
     */
    public static void runOnMainThreadNowOrLater(@NonNull Runnable runnable) {
        if (isCurrentlyOnMainThread()) {
            runnable.run();
        } else {
            runOnMainThread(runnable);
        }
    }

    /**
     * @return if the calling thread is on the main thread
     */
    public static boolean isCurrentlyOnMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }

    /**
     * @throws IllegalStateException if the calling thread is _off_ the main thread
     */
    public static void verifyOnMainThread() throws IllegalStateException {
        if (!isCurrentlyOnMainThread()) {
            throw new IllegalStateException("Must call _on_ the main thread");
        }
    }

    /**
     * @throws IllegalStateException if the calling thread is _on_ the main thread
     */
    public static void verifyOffMainThread() throws IllegalStateException {
        if (isCurrentlyOnMainThread()) {
            throw new IllegalStateException("Must call _off_ the main thread");
        }
    }

    public static boolean isNetworkConnected() {
        NetworkType networkType = getNetworkType();
        return networkType == NetworkType.MOBILE
                || networkType == NetworkType.OTHER;
    }

    @SuppressLint("MissingPermission") // permission already included in YouTube
    public static NetworkType getNetworkType() {
        Context networkContext = getContext();
        if (networkContext == null) {
            return NetworkType.NONE;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        var networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return NetworkType.NONE;
        }
        var type = networkInfo.getType();
        return (type == ConnectivityManager.TYPE_MOBILE)
                || (type == ConnectivityManager.TYPE_BLUETOOTH) ? NetworkType.MOBILE : NetworkType.OTHER;
    }

    /**
     * Hide a view by setting its layout params to 1x1
     * @param view The view to hide.
     */
    public static void hideViewByLayoutParams(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams);
        } else if (view instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams2);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams3);
        } else if (view instanceof Toolbar) {
            Toolbar.LayoutParams layoutParams4 = new Toolbar.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams4);
        } else if (view instanceof ViewGroup) {
            ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(1, 1);
            view.setLayoutParams(layoutParams5);
        } else {
            LogHelper.printDebug(() -> "Hidden view with id " + view.getId());
        }
    }

    public enum NetworkType {
        NONE,
        MOBILE,
        OTHER,
    }
}