package app.revanced.extension.shared;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.preference.ReVancedAboutPreference;

public class Utils {

    @SuppressLint("StaticFieldLeak")
    private static volatile Context context;

    private static String versionName;
    private static String applicationLabel;

    private static int darkColor = Color.BLACK; // Default dark color.
    private static int lightColor = Color.WHITE; // Default light color.

    private Utils() {
    } // utility class

    /**
     * Injection point.
     *
     * @return The manifest 'Version' entry of the patches.jar used during patching.
     */
    @SuppressWarnings("SameReturnValue")
    public static String getPatchesReleaseVersion() {
        return ""; // Value is replaced during patching.
    }

    private static PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        final var packageName = Objects.requireNonNull(getContext()).getPackageName();

        PackageManager packageManager = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
            );
        }

        return packageManager.getPackageInfo(
                packageName,
                0
        );
    }

    /**
     * @return The version name of the app, such as 19.11.43
     */
    public static String getAppVersionName() {
        if (versionName == null) {
            try {
                versionName = getPackageInfo().versionName;
            } catch (Exception ex) {
                Logger.printException(() -> "Failed to get package info", ex);
                versionName = "Unknown";
            }
        }

        return versionName;
    }

    public static String getApplicationName() {
        if (applicationLabel == null) {
            try {
                ApplicationInfo applicationInfo = getPackageInfo().applicationInfo;
                applicationLabel = (String) applicationInfo.loadLabel(context.getPackageManager());
            } catch (Exception ex) {
                Logger.printException(() -> "Failed to get application name", ex);
                applicationLabel = "Unknown";
            }
        }

        return applicationLabel;
    }

    /**
     * Hide a view by setting its layout height and width to 1dp.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    public static void hideViewBy0dpUnderCondition(BooleanSetting condition, View view) {
        if (hideViewBy0dpUnderCondition(condition.get(), view)) {
            Logger.printDebug(() -> "View hidden by setting: " + condition);
        }
    }

    /**
     * Hide a view by setting its layout height and width to 0dp.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    public static boolean hideViewBy0dpUnderCondition(boolean condition, View view) {
        if (condition) {
            hideViewByLayoutParams(view);
            return true;
        }

        return false;
    }

    /**
     * Hide a view by setting its visibility to GONE.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    public static void hideViewUnderCondition(BooleanSetting condition, View view) {
        if (hideViewUnderCondition(condition.get(), view)) {
            Logger.printDebug(() -> "View hidden by setting: " + condition);
        }
    }

    /**
     * Hide a view by setting its visibility to GONE.
     *
     * @param condition The setting to check for hiding the view.
     * @param view      The view to hide.
     */
    public static boolean hideViewUnderCondition(boolean condition, View view) {
        if (condition) {
            view.setVisibility(View.GONE);
            return true;
        }

        return false;
    }

    public static void hideViewByRemovingFromParentUnderCondition(BooleanSetting condition, View view) {
        if (hideViewByRemovingFromParentUnderCondition(condition.get(), view)) {
            Logger.printDebug(() -> "View hidden by setting: " + condition);
        }
    }

    public static boolean hideViewByRemovingFromParentUnderCondition(boolean setting, View view) {
        if (setting) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(view);
                return true;
            }
        }

        return false;
    }

    /**
     * General purpose pool for network calls and other background tasks.
     * All tasks run at max thread priority.
     */
    private static final ThreadPoolExecutor backgroundThreadPool = new ThreadPoolExecutor(
            3, // 3 threads always ready to go
            Integer.MAX_VALUE,
            10, // For any threads over the minimum, keep them alive 10 seconds after they go idle
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            r -> { // ThreadFactory
                Thread t = new Thread(r);
                t.setPriority(Thread.MAX_PRIORITY); // run at max priority
                return t;
            });

    public static void runOnBackgroundThread(Runnable task) {
        backgroundThreadPool.execute(task);
    }

    public static <T> Future<T> submitOnBackgroundThread(Callable<T> call) {
        return backgroundThreadPool.submit(call);
    }

    /**
     * Simulates a delay by doing meaningless calculations.
     * Used for debugging to verify UI timeout logic.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static long doNothingForDuration(long amountOfTimeToWaste) {
        final long timeCalculationStarted = System.currentTimeMillis();
        Logger.printDebug(() -> "Artificially creating delay of: " + amountOfTimeToWaste + "ms");

        long meaninglessValue = 0;
        while (System.currentTimeMillis() - timeCalculationStarted < amountOfTimeToWaste) {
            // could do a thread sleep, but that will trigger an exception if the thread is interrupted
            meaninglessValue += Long.numberOfLeadingZeros((long) Math.exp(Math.random()));
        }
        // return the value, otherwise the compiler or VM might optimize and remove the meaningless time wasting work,
        // leaving an empty loop that hammers on the System.currentTimeMillis native call
        return meaninglessValue;
    }

    public static boolean containsAny(String value, String... targets) {
        return indexOfFirstFound(value, targets) >= 0;
    }

    public static int indexOfFirstFound(String value, String... targets) {
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
    public static int getResourceIdentifier(Context context, String resourceIdentifierName, String type) {
        return context.getResources().getIdentifier(resourceIdentifierName, type, context.getPackageName());
    }

    /**
     * @return zero, if the resource is not found
     */
    public static int getResourceIdentifier(String resourceIdentifierName, String type) {
        return getResourceIdentifier(getContext(), resourceIdentifierName, type);
    }

    public static int getResourceInteger(String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getInteger(getResourceIdentifier(resourceIdentifierName, "integer"));
    }

    public static Animation getResourceAnimation(String resourceIdentifierName) throws Resources.NotFoundException {
        return AnimationUtils.loadAnimation(getContext(), getResourceIdentifier(resourceIdentifierName, "anim"));
    }

    public static int getResourceColor(String resourceIdentifierName) throws Resources.NotFoundException {
        //noinspection deprecation
        return getContext().getResources().getColor(getResourceIdentifier(resourceIdentifierName, "color"));
    }

    public static int getResourceDimensionPixelSize(String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getDimensionPixelSize(getResourceIdentifier(resourceIdentifierName, "dimen"));
    }

    public static float getResourceDimension(String resourceIdentifierName) throws Resources.NotFoundException {
        return getContext().getResources().getDimension(getResourceIdentifier(resourceIdentifierName, "dimen"));
    }

    public interface MatchFilter<T> {
        boolean matches(T object);
    }

    /**
     * Includes sub children.
     *
     * @noinspection unchecked
     */
    public static <R extends View> R getChildViewByResourceName(View view, String str) {
        var child = view.findViewById(Utils.getResourceIdentifier(str, "id"));
        if (child != null) {
            return (R) child;
        }

        throw new IllegalArgumentException("View with resource name '" + str + "' not found");
    }

    /**
     * @param searchRecursively If children ViewGroups should also be
     *                          recursively searched using depth first search.
     * @return The first child view that matches the filter.
     */
    @Nullable
    public static <T extends View> T getChildView(ViewGroup viewGroup, boolean searchRecursively,
                                                  MatchFilter<View> filter) {
        for (int i = 0, childCount = viewGroup.getChildCount(); i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);

            if (filter.matches(childAt)) {
                //noinspection unchecked
                return (T) childAt;
            }
            // Must do recursive after filter check, in case the filter is looking for a ViewGroup.
            if (searchRecursively && childAt instanceof ViewGroup) {
                T match = getChildView((ViewGroup) childAt, true, filter);
                if (match != null) return match;
            }
        }

        return null;
    }

    @Nullable
    public static ViewParent getParentView(View view, int nthParent) {
        ViewParent parent = view.getParent();

        int currentDepth = 0;
        while (++currentDepth < nthParent && parent != null) {
            parent = parent.getParent();
        }

        if (currentDepth == nthParent) {
            return parent;
        }

        final int currentDepthLog = currentDepth;
        Logger.printDebug(() -> "Could not find parent view of depth: " + nthParent
                + " and instead found at: " + currentDepthLog + " view: " + view);
        return null;
    }

    public static void restartApp(Context context) {
        String packageName = context.getPackageName();
        Intent intent = Objects.requireNonNull(context.getPackageManager().getLaunchIntentForPackage(packageName));
        Intent mainIntent = Intent.makeRestartActivityTask(intent.getComponent());
        // Required for API 34 and later
        // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
        mainIntent.setPackage(packageName);
        context.startActivity(mainIntent);
        System.exit(0);
    }

    public static Context getContext() {
        if (context == null) {
            Logger.initializationException(() -> "Context is not set by extension hook, returning null",  null);
        }
        return context;
    }

    public static void setContext(Context appContext) {
        // Intentionally use logger before context is set,
        // to expose any bugs in the 'no context available' logger method.
        Logger.initializationInfo(() -> "Set context: " + appContext);
        // Must initially set context to check the app language.
        context = appContext;

        AppLanguage language = BaseSettings.REVANCED_LANGUAGE.get();
        if (language != AppLanguage.DEFAULT) {
            // Create a new context with the desired language.
            Logger.printDebug(() -> "Using app language: " + language);
            Configuration config = new Configuration(appContext.getResources().getConfiguration());
            config.setLocale(language.getLocale());
            context = appContext.createConfigurationContext(config);
        }
    }

    public static void setClipboard(CharSequence text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("ReVanced", text);
        clipboard.setPrimaryClip(clip);
    }

    public static boolean isTablet() {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    @Nullable
    private static Boolean isRightToLeftTextLayout;

    /**
     * @return If the device language uses right to left text layout (Hebrew, Arabic, etc).
     *         If this should match any ReVanced language override then instead use
     *         {@link #isRightToLeftLocale(Locale)} with {@link BaseSettings#REVANCED_LANGUAGE}.
     *         This is the default locale of the device, which may differ if
     *         {@link BaseSettings#REVANCED_LANGUAGE} is set to a different language.
     */
    public static boolean isRightToLeftLocale() {
        if (isRightToLeftTextLayout == null) {
            isRightToLeftTextLayout = isRightToLeftLocale(Locale.getDefault());
        }
        return isRightToLeftTextLayout;
    }

    /**
     * @return If the locale uses right to left text layout (Hebrew, Arabic, etc).
     */
    public static boolean isRightToLeftLocale(Locale locale) {
        String displayLanguage = locale.getDisplayLanguage();
        return new Bidi(displayLanguage, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isRightToLeft();
    }

    /**
     * @return A UTF8 string containing a left-to-right or right-to-left
     *         character of the device locale. If this should match any ReVanced language
     *         override then instead use {@link #getTextDirectionString(Locale)} with
     *         {@link BaseSettings#REVANCED_LANGUAGE}.
     */
    public static String getTextDirectionString() {
        return  getTextDirectionString(isRightToLeftLocale());
    }

    public static String getTextDirectionString(Locale locale) {
        return getTextDirectionString(isRightToLeftLocale(locale));
    }

    private static String getTextDirectionString(boolean isRightToLeft) {
        return isRightToLeft
                ? "\u200F"  // u200F = right to left character.
                : "\u200E"; // u200E = left to right character.
    }

    /**
     * @return if the text contains at least 1 number character,
     *         including any unicode numbers such as Arabic.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean containsNumber(CharSequence text) {
        for (int index = 0, length = text.length(); index < length;) {
            final int codePoint = Character.codePointAt(text, index);
            if (Character.isDigit(codePoint)) {
                return true;
            }
            index += Character.charCount(codePoint);
        }

        return false;
    }

    /**
     * Ignore this class. It must be public to satisfy Android requirements.
     */
    @SuppressWarnings("deprecation")
    public static final class DialogFragmentWrapper extends DialogFragment {

        private Dialog dialog;
        @Nullable
        private DialogFragmentOnStartAction onStartAction;

        @Override
        public void onSaveInstanceState(Bundle outState) {
            // Do not call super method to prevent state saving.
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return dialog;
        }

        @Override
        public void onStart() {
            try {
                super.onStart();

                if (onStartAction != null) {
                    onStartAction.onStart(getDialog());
                }
            } catch (Exception ex) {
                Logger.printException(() -> "onStart failure: " + dialog.getClass().getSimpleName(), ex);
            }
        }
    }

    /**
     * Interface for {@link #showDialog(Activity, Dialog, boolean, DialogFragmentOnStartAction)}.
     */
    @FunctionalInterface
    public interface DialogFragmentOnStartAction {
        void onStart(Dialog dialog);
    }

    public static void showDialog(Activity activity, Dialog dialog) {
        showDialog(activity, dialog, true, null);
    }

    /**
     * Utility method to allow showing a Dialog on top of other dialogs.
     * Calling this will always display the dialog on top of all other dialogs
     * previously called using this method.
     * <br>
     * Be aware the on start action can be called multiple times for some situations,
     * such as the user switching apps without dismissing the dialog then switching back to this app.
     * <br>
     * This method is only useful during app startup and multiple patches may show their own dialog,
     * and the most important dialog can be called last (using a delay) so it's always on top.
     * <br>
     * For all other situations it's better to not use this method and
     * call {@link Dialog#show()} on the dialog.
     */
    @SuppressWarnings("deprecation")
    public static void showDialog(Activity activity,
                                  Dialog dialog,
                                  boolean isCancelable,
                                  @Nullable DialogFragmentOnStartAction onStartAction) {
        verifyOnMainThread();

        DialogFragmentWrapper fragment = new DialogFragmentWrapper();
        fragment.dialog = dialog;
        fragment.onStartAction = onStartAction;
        fragment.setCancelable(isCancelable);

        fragment.show(activity.getFragmentManager(), null);
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastShort(String messageToToast) {
        showToast(messageToToast, Toast.LENGTH_SHORT);
    }

    /**
     * Safe to call from any thread
     */
    public static void showToastLong(String messageToToast) {
        showToast(messageToToast, Toast.LENGTH_LONG);
    }

    private static void showToast(String messageToToast, int toastDuration) {
        Objects.requireNonNull(messageToToast);
        runOnMainThreadNowOrLater(() -> {
            Context currentContext = context;

            if (currentContext == null) {
                Logger.initializationException(() -> "Cannot show toast (context is null): " + messageToToast, null);
            } else {
                Logger.printDebug(() -> "Showing toast: " + messageToToast);
                Toast.makeText(currentContext, messageToToast, toastDuration).show();
            }
        });
    }

    public static boolean isDarkModeEnabled() {
        Configuration config = Resources.getSystem().getConfiguration();
        final int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isLandscapeOrientation() {
        final int orientation = Resources.getSystem().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Automatically logs any exceptions the runnable throws.
     *
     * @see #runOnMainThreadNowOrLater(Runnable)
     */
    public static void runOnMainThread(Runnable runnable) {
        runOnMainThreadDelayed(runnable, 0);
    }

    /**
     * Automatically logs any exceptions the runnable throws.
     */
    public static void runOnMainThreadDelayed(Runnable runnable, long delayMillis) {
        Runnable loggingRunnable = () -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                Logger.printException(() -> runnable.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
            }
        };
        new Handler(Looper.getMainLooper()).postDelayed(loggingRunnable, delayMillis);
    }

    /**
     * If called from the main thread, the code is run immediately.<p>
     * If called off the main thread, this is the same as {@link #runOnMainThread(Runnable)}.
     */
    public static void runOnMainThreadNowOrLater(Runnable runnable) {
        if (isCurrentlyOnMainThread()) {
            runnable.run();
        } else {
            runOnMainThread(runnable);
        }
    }

    /**
     * @return if the calling thread is on the main thread.
     */
    public static boolean isCurrentlyOnMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }

    /**
     * @throws IllegalStateException if the calling thread is _off_ the main thread.
     */
    public static void verifyOnMainThread() throws IllegalStateException {
        if (!isCurrentlyOnMainThread()) {
            throw new IllegalStateException("Must call _on_ the main thread");
        }
    }

    /**
     * @throws IllegalStateException if the calling thread is _on_ the main thread.
     */
    public static void verifyOffMainThread() throws IllegalStateException {
        if (isCurrentlyOnMainThread()) {
            throw new IllegalStateException("Must call _off_ the main thread");
        }
    }

    public enum NetworkType {
        NONE,
        MOBILE,
        OTHER,
    }

    /**
     * Calling extension code must ensure the un-patched app has the permission
     * <code>android.permission.ACCESS_NETWORK_STATE</code>, otherwise the app will crash
     * if this method is used.
     */
    public static boolean isNetworkConnected() {
        NetworkType networkType = getNetworkType();
        return networkType == NetworkType.MOBILE
                || networkType == NetworkType.OTHER;
    }

    /**
     * Calling extension code must ensure the un-patched app has the permission
     * <code>android.permission.ACCESS_NETWORK_STATE</code>, otherwise the app will crash
     * if this method is used.
     */
    @SuppressLint({"MissingPermission", "deprecation"})
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
     * Hide a view by setting its layout params to 0x0
     * @param view The view to hide.
     */
    public static void hideViewByLayoutParams(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams);
        } else if (view instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams2);
        } else if (view instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams3);
        } else if (view instanceof Toolbar) {
            Toolbar.LayoutParams layoutParams4 = new Toolbar.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams4);
        } else if (view instanceof ViewGroup) {
            ViewGroup.LayoutParams layoutParams5 = new ViewGroup.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams5);
        } else {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = 0;
            params.height = 0;
            view.setLayoutParams(params);
        }
    }

    /**
     * Creates a custom dialog with a styled layout, including a title, message, buttons, and an optional EditText.
     * The dialog's appearance adapts to the app's dark mode setting, with rounded corners and customizable button actions.
     * Buttons adjust dynamically to their text content and are arranged in a single row if they fit within 80% of the screen width,
     * with the Neutral button aligned to the left and OK/Cancel buttons centered on the right.
     * If buttons do not fit, each is placed on a separate row, all aligned to the right.
     *
     * @param context           Context used to create the dialog.
     * @param title             Title text of the dialog.
     * @param message           Message text of the dialog (supports Spanned for HTML), or null if replaced by EditText.
     * @param editText          EditText to include in the dialog, or null if no EditText is needed.
     * @param okButtonText      OK button text, or null to use the default "OK" string.
     * @param onOkClick         Action to perform when the OK button is clicked.
     * @param onCancelClick     Action to perform when the Cancel button is clicked, or null if no Cancel button is needed.
     * @param neutralButtonText Neutral button text, or null if no Neutral button is needed.
     * @param onNeutralClick    Action to perform when the Neutral button is clicked, or null if no Neutral button is needed.
     * @param dismissDialogOnNeutralClick If the dialog should be dismissed when the Neutral button is clicked.
     * @return The Dialog and its main LinearLayout container.
     */
    @SuppressWarnings("ExtractMethodRecommender")
    public static Pair<Dialog, LinearLayout> createCustomDialog(
            Context context, String title, CharSequence message, @Nullable EditText editText,
            String okButtonText, Runnable onOkClick, Runnable onCancelClick,
            @Nullable String neutralButtonText, @Nullable Runnable onNeutralClick,
            boolean dismissDialogOnNeutralClick
    ) {
        Logger.printDebug(() -> "Creating custom dialog with title: " + title);

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove default title bar.

        // Create main layout.
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Preset size constants.
        final int dip8 = dipToPixels(8);
        final int dip16 = dipToPixels(16);
        final int dip28 = dipToPixels(28); // Padding for mainLayout.

        mainLayout.setPadding(dip28, dip16, dip28, dip28);
        // Set rounded rectangle background.
        ShapeDrawable mainBackground = new ShapeDrawable(new RoundRectShape(
                createCornerRadii(28), null, null));
        mainBackground.getPaint().setColor(getDialogBackgroundColor()); // Dialog background.
        mainLayout.setBackground(mainBackground);

        // Title.
        if (!TextUtils.isEmpty(title)) {
            TextView titleView = new TextView(context);
            titleView.setText(title);
            titleView.setTypeface(Typeface.DEFAULT_BOLD);
            titleView.setTextSize(18);
            titleView.setTextColor(getAppForegroundColor());
            titleView.setPadding(0, 0, 0, dip8);
            titleView.setGravity(Gravity.CENTER);
            // Set layout parameters to match parent width and wrap content height.
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            titleView.setLayoutParams(layoutParams);
            mainLayout.addView(titleView);
        }

        // Message (if not replaced by EditText).
        if (editText == null && message != null) {
            TextView messageView = new TextView(context);
            messageView.setText(message); // Supports Spanned (HTML).
            messageView.setTextSize(16);
            messageView.setTextColor(getAppForegroundColor());
            messageView.setPadding(0, dip8, 0, dip16);
            // Enable HTML link clicking if the message contains links.
            if (message instanceof Spanned) {
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            mainLayout.addView(messageView);
        }

        // EditText (if provided).
        if (editText != null) {
            // Remove EditText from its current parent, if any.
            ViewGroup parent = (ViewGroup) editText.getParent();
            if (parent != null) {
                parent.removeView(editText);
            }
            // Style the EditText to match the dialog theme.
            editText.setTextColor(getAppForegroundColor());
            editText.setBackgroundColor(isDarkModeEnabled() ? Color.BLACK : Color.WHITE);
            editText.setPadding(dip8, dip8, dip8, dip8);
            ShapeDrawable editTextBackground = new ShapeDrawable(new RoundRectShape(
                    createCornerRadii(10), null, null));
            editTextBackground.getPaint().setColor(getEditTextBackground()); // Background color for EditText.
            editText.setBackground(editTextBackground);

            LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editTextParams.setMargins(0, dip8, 0, dip8);
            // Prevent buttons from moving off the screen by fixing the height of the EditText.
            final int maxHeight = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.6);
            editText.setMaxHeight(maxHeight);
            mainLayout.addView(editText, 1, editTextParams);
        }

        // Button container.
        LinearLayout buttonContainer = new LinearLayout(context);
        buttonContainer.setOrientation(LinearLayout.VERTICAL); // Default to vertical to allow wrapping.
        LinearLayout.LayoutParams buttonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonContainerParams.setMargins(0, dip8, 0, 0);
        buttonContainer.setLayoutParams(buttonContainerParams);

        // Lists to track buttons
        List<Button> buttons = new ArrayList<>();
        List<Integer> buttonWidths = new ArrayList<>();

        if (neutralButtonText != null && onNeutralClick != null) {
            LinearLayout neutralContainer = new LinearLayout(context);
            neutralContainer.setOrientation(LinearLayout.HORIZONTAL);
            neutralContainer.setGravity(Gravity.END); // Align Neutral to right when on separate row
            Button neutralButton = addButton(
                    neutralContainer,
                    context,
                    neutralButtonText,
                    onNeutralClick,
                    false,
                    false,
                    true,
                    dismissDialogOnNeutralClick,
                    false,
                    dialog
            );
            neutralContainer.addView(neutralButton);
            buttons.add(neutralButton);
            neutralButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            buttonWidths.add(neutralButton.getMeasuredWidth());
            buttonContainer.addView(neutralContainer);
        }

        if (onCancelClick != null) {
            LinearLayout cancelContainer = new LinearLayout(context);
            cancelContainer.setOrientation(LinearLayout.HORIZONTAL);
            cancelContainer.setGravity(Gravity.END);
            Button cancelButton = addButton(
                    cancelContainer,
                    context,
                    context.getString(android.R.string.cancel),
                    onCancelClick,
                    false,
                    true,
                    false,
                    true,
                    false,
                    dialog
            );
            cancelContainer.addView(cancelButton);
            buttons.add(cancelButton);
            cancelButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            buttonWidths.add(cancelButton.getMeasuredWidth());
            buttonContainer.addView(cancelContainer);
        }

        if (onOkClick != null) {
            LinearLayout okContainer = new LinearLayout(context);
            okContainer.setOrientation(LinearLayout.HORIZONTAL);
            okContainer.setGravity(Gravity.END);
            Button okButton = addButton(
                    okContainer,
                    context,
                    okButtonText != null ? okButtonText : context.getString(android.R.string.ok),
                    onOkClick,
                    true,
                    false,
                    false,
                    true,
                    false,
                    dialog
            );
            okContainer.addView(okButton);
            buttons.add(okButton);
            okButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            buttonWidths.add(okButton.getMeasuredWidth());
            buttonContainer.addView(okContainer);
        }

        // Check if buttons fit in one row.
        int totalWidth = 0;
        for (Integer width : buttonWidths) {
            totalWidth += width + dipToPixels(8); // Include margins.
        }
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        if (totalWidth <= screenWidth * 0.8 && buttons.size() > 1) {
            // Clear vertical container and use a single row.
            buttonContainer.removeAllViews();
            LinearLayout rowContainer = new LinearLayout(context);
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            rowContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Add Neutral button.
            if (neutralButtonText != null && onNeutralClick != null) {
                LinearLayout neutralContainer = new LinearLayout(context);
                neutralContainer.setOrientation(LinearLayout.HORIZONTAL);
                neutralContainer.setGravity(Gravity.START); // Neutral to left in single row
                Button neutralButton = buttons.get(0);
                ViewGroup parent = (ViewGroup) neutralButton.getParent();
                if (parent != null) {
                    parent.removeView(neutralButton);
                }
                // Update margins for Neutral button in single row.
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) neutralButton.getLayoutParams();
                params.setMargins(0, dipToPixels(4), dipToPixels(4), dipToPixels(4));
                neutralButton.setLayoutParams(params);
                neutralContainer.addView(neutralButton);
                rowContainer.addView(neutralContainer);
            }

            // Add spacer.
            LinearLayout spacer = new LinearLayout(context);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
            rowContainer.addView(spacer);

            // Add OK and Cancel buttons (right-aligned).
            LinearLayout okCancelContainer = new LinearLayout(context);
            okCancelContainer.setOrientation(LinearLayout.HORIZONTAL);
            okCancelContainer.setGravity(Gravity.END);
            for (int i = (neutralButtonText != null && onNeutralClick != null) ? 1 : 0; i < buttons.size(); i++) {
                Button button = buttons.get(i);
                ViewGroup parent = (ViewGroup) button.getParent();
                if (parent != null) {
                    parent.removeView(button);
                }
                // Update margins for OK button in single row.
                if (i == buttons.size() - 1 && onOkClick != null) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
                    params.setMargins(dipToPixels(4), dipToPixels(4), 0, dipToPixels(4));
                    button.setLayoutParams(params);
                }
                okCancelContainer.addView(button);
            }
            rowContainer.addView(okCancelContainer);
            buttonContainer.addView(rowContainer);
        } else {
            // Update margins for each button in separate rows.
            for (Button button : buttons) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
                params.setMargins(0, dipToPixels(4), 0, dipToPixels(4));
                button.setLayoutParams(params);
            }
        }

        mainLayout.addView(buttonContainer);
        dialog.setContentView(mainLayout);

        // Set dialog window attributes.
        Window window = dialog.getWindow();
        if (window != null) {
            setDialogWindowParameters(context, window);
        }

        return new Pair<>(dialog, mainLayout);
    }

    public static void setDialogWindowParameters(Context context, Window window) {
        WindowManager.LayoutParams params = window.getAttributes();

        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int portraitWidth = (int) (displayMetrics.widthPixels * 0.9);
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            portraitWidth = (int) Math.min(portraitWidth, displayMetrics.heightPixels * 0.9);
        }
        params.width = portraitWidth;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        window.setBackgroundDrawable(null); // Remove default dialog background.
    }

    /**
     * Adds a styled button to a dialog's button container with customizable text, click behavior, and appearance.
     * The button's background and text colors adapt to the app's dark mode setting. Buttons have dynamic width
     * based on text content. In a single-row layout, the Neutral button is left-aligned, while OK and
     * Cancel are right-aligned. When wrapped to separate rows, all buttons are right-aligned.
     *
     * @param buttonContainer Container where the button will be added.
     * @param context         Context to create the button and access resources.
     * @param buttonText      Button text to display.
     * @param onClick         Action to perform when the button is clicked, or null if no action is required.
     * @param isOkButton      If this is the OK button, which uses distinct background and text colors.
     * @param isCancelButton  If this is the Cancel button, which uses distinct background and text colors.
     * @param isNeutralButton If this is a Neutral button, affecting alignment in single-row layout.
     * @param dismissDialog   If the dialog should be dismissed when the button is clicked.
     * @param isSingleRow     If the button is part of a single-row layout.
     * @param dialog          The Dialog to dismiss when the button is clicked.
     * @return The created Button.
     */
    private static Button addButton(
            LinearLayout buttonContainer,
            Context context,
            String buttonText,
            Runnable onClick,
            boolean isOkButton,
            boolean isCancelButton,
            boolean isNeutralButton,
            boolean dismissDialog,
            boolean isSingleRow,
            Dialog dialog) {

        Button button = new Button(context, null, 0);
        button.setText(buttonText);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setSingleLine(true);
        button.setEllipsize(android.text.TextUtils.TruncateAt.END);
        button.setGravity(Gravity.CENTER);

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(createCornerRadii(20), null, null));
        int backgroundColor = isOkButton
                ? getOkButtonBackgroundColor() // Background color for OK button (inversion).
                : getCancelOrNeutralButtonBackgroundColor(); // Background color for Cancel or Neutral buttons.
        background.getPaint().setColor(backgroundColor);
        button.setBackground(background);

        button.setTextColor(isDarkModeEnabled()
                ? (isOkButton ? Color.BLACK : Color.WHITE)
                : (isOkButton ? Color.WHITE : Color.BLACK));

        // Set internal padding.
        button.setPadding(dipToPixels(16), 0, dipToPixels(16), 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dipToPixels(36)
        );
        // Set margins based on layout and button type
        if (isSingleRow) {
            if (isNeutralButton) {
                params.setMargins(0, dipToPixels(4), dipToPixels(4), dipToPixels(4));
            } else if (isOkButton) {
                params.setMargins(dipToPixels(4), dipToPixels(4), 0, dipToPixels(4));
            } else {
                params.setMargins(dipToPixels(4), dipToPixels(4), dipToPixels(4), dipToPixels(4));
            }
        } else {
            params.setMargins(0, dipToPixels(4), 0, dipToPixels(4));
        }

        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.run();
            }
            if (dismissDialog) {
                dialog.dismiss();
            }
        });

        return button;
    }

    /**
     * Creates an array of corner radii for a rounded rectangle shape.
     *
     * @param dp Radius in density-independent pixels (dip) to apply to all corners.
     * @return An array of eight float values representing the corner radii
     * (top-left, top-right, bottom-right, bottom-left).
     */
    public static float[] createCornerRadii(float dp) {
        final float radius = dipToPixels(dp);
        return new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
    }

    /**
     * Sets the theme dark used by the app.
     */
    public static void setThemeDarkColor(@ColorInt int color) {
        Logger.printDebug(() -> "Setting theme dark color: " + getColorHexString(color));
        darkColor = color;
    }

    /**
     * Sets the theme light color used by the app.
     */
    public static void setThemeLightColor(@ColorInt int color) {
        Logger.printDebug(() -> "Setting theme light color: " + getColorHexString(color));
        lightColor = color;
    }

    /**
     * Returns the themed dark color, or {@link Color#BLACK} if no theme was set using
     * {@link #setThemeDarkColor(int)}.
     */
    @ColorInt
    public static int getThemeDarkColor() {
        return darkColor;
    }

    /**
     * Returns the themed light color, or {@link Color#WHITE} if no theme was set using
     * {@link #setThemeLightColor(int).
     */
    @ColorInt
    public static int getThemeLightColor() {
        return lightColor;
    }

    @ColorInt
    public static int getDialogBackgroundColor() {
        if (isDarkModeEnabled()) {
            final int darkColor = getThemeDarkColor();
            return darkColor == Color.BLACK
                    // Lighten the background a little if using AMOLED dark theme
                    // as the dialogs are almost invisible.
                    ? 0xFF0D0D0D
                    : darkColor;
        }
        return getThemeLightColor();
    }

    /**
     * @return The current app background color.
     */
    @ColorInt
    public static int getAppBackgroundColor() {
        return isDarkModeEnabled() ? getThemeDarkColor() : getThemeLightColor();
    }

    /**
     * @return The current app foreground color.
     */
    @ColorInt
    public static int getAppForegroundColor() {
        return isDarkModeEnabled()
                ? getThemeLightColor()
                : getThemeDarkColor();
    }

    @ColorInt
    public static int getOkButtonBackgroundColor() {
        return isDarkModeEnabled()
                // Must be inverted color.
                ? Color.WHITE
                : Color.BLACK;
    }

    @ColorInt
    public static int getCancelOrNeutralButtonBackgroundColor() {
        return isDarkModeEnabled()
                ? adjustColorBrightness(getDialogBackgroundColor(), 1.10f)
                : adjustColorBrightness(getThemeLightColor(), 0.95f);
    }

    @ColorInt
    public static int getEditTextBackground() {
        return isDarkModeEnabled()
                ? adjustColorBrightness(getDialogBackgroundColor(), 1.05f)
                : adjustColorBrightness(getThemeLightColor(), 0.97f);
    }

    public static String getColorHexString(@ColorInt int color) {
        return String.format("#%06X", (0x00FFFFFF & color));
    }

    /**
     * {@link PreferenceScreen} and {@link PreferenceGroup} sorting styles.
     */
    private enum Sort {
        /**
         * Sort by the localized preference title.
         */
        BY_TITLE("_sort_by_title"),

        /**
         * Sort by the preference keys.
         */
        BY_KEY("_sort_by_key"),

        /**
         * Unspecified sorting.
         */
        UNSORTED("_sort_by_unsorted");

        final String keySuffix;

        Sort(String keySuffix) {
            this.keySuffix = keySuffix;
        }

        static Sort fromKey(@Nullable String key, Sort defaultSort) {
            if (key != null) {
                for (Sort sort : values()) {
                    if (key.endsWith(sort.keySuffix)) {
                        return sort;
                    }
                }
            }
            return defaultSort;
        }
    }

    private static final Pattern punctuationPattern = Pattern.compile("\\p{P}+");

    /**
     * Strips all punctuation and converts to lower case.  A null parameter returns an empty string.
     */
    public static String removePunctuationToLowercase(@Nullable CharSequence original) {
        if (original == null) return "";
        return punctuationPattern.matcher(original).replaceAll("")
                .toLowerCase(BaseSettings.REVANCED_LANGUAGE.get().getLocale());
    }

    /**
     * Sort a PreferenceGroup and all it's sub groups by title or key.
     *
     * Sort order is determined by the preferences key {@link Sort} suffix.
     *
     * If a preference has no key or no {@link Sort} suffix,
     * then the preferences are left unsorted.
     */
    @SuppressWarnings("deprecation")
    public static void sortPreferenceGroups(PreferenceGroup group) {
        Sort groupSort = Sort.fromKey(group.getKey(), Sort.UNSORTED);
        List<Pair<String, Preference>> preferences = new ArrayList<>();

        for (int i = 0, prefCount = group.getPreferenceCount(); i < prefCount; i++) {
            Preference preference = group.getPreference(i);

            final Sort preferenceSort;
            if (preference instanceof PreferenceGroup subGroup) {
                sortPreferenceGroups(subGroup);
                preferenceSort = groupSort; // Sort value for groups is for it's content, not itself.
            } else {
                // Allow individual preferences to set a key sorting.
                // Used to force a preference to the top or bottom of a group.
                preferenceSort = Sort.fromKey(preference.getKey(), groupSort);
            }

            final String sortValue;
            switch (preferenceSort) {
                case BY_TITLE:
                    sortValue = removePunctuationToLowercase(preference.getTitle());
                    break;
                case BY_KEY:
                    sortValue = preference.getKey();
                    break;
                case UNSORTED:
                    continue; // Keep original sorting.
                default:
                    throw new IllegalStateException();
            }

            preferences.add(new Pair<>(sortValue, preference));
        }

        //noinspection ComparatorCombinators
        Collections.sort(preferences, (pair1, pair2)
                -> pair1.first.compareTo(pair2.first));

        int index = 0;
        for (Pair<String, Preference> pair : preferences) {
            int order = index++;
            Preference pref = pair.second;

            // Move any screens, intents, and the one off About preference to the top.
            if (pref instanceof PreferenceScreen || pref instanceof ReVancedAboutPreference
                    || pref.getIntent() != null) {
                // Any arbitrary large number.
                order -= 1000;
            }

            pref.setOrder(order);
        }
    }

    /**
     * Set all preferences to multiline titles if the device is not using an English variant.
     * The English strings are heavily scrutinized and all titles fit on screen
     * except 2 or 3 preference strings and those do not affect readability.
     *
     * Allowing multiline for those 2 or 3 English preferences looks weird and out of place,
     * and visually it looks better to clip the text and keep all titles 1 line.
     */
    @SuppressWarnings("deprecation")
    public static void setPreferenceTitlesToMultiLineIfNeeded(PreferenceGroup group) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        String revancedLocale = Utils.getContext().getResources().getConfiguration().locale.getLanguage();
        if (revancedLocale.equals(Locale.ENGLISH.getLanguage())) {
            return;
        }

        for (int i = 0, prefCount = group.getPreferenceCount(); i < prefCount; i++) {
            Preference pref = group.getPreference(i);
            pref.setSingleLineTitle(false);

            if (pref instanceof PreferenceGroup subGroup) {
                setPreferenceTitlesToMultiLineIfNeeded(subGroup);
            }
        }
    }

    /**
     * Parse a color resource or hex code to an int representation of the color.
     */
    @ColorInt
    public static int getColorFromString(String colorString) throws IllegalArgumentException, Resources.NotFoundException {
        if (colorString.startsWith("#")) {
            return Color.parseColor(colorString);
        }
        return getResourceColor(colorString);
    }

    /**
     * Converts dip value to actual device pixels.
     *
     * @param dip The density-independent pixels value
     * @return The device pixel value
     */
    public static int dipToPixels(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                Resources.getSystem().getDisplayMetrics()
        );
    }

    /**
     * Adjusts the brightness of a color by lightening or darkening it based on the given factor.
     * <p>
     * If the factor is greater than 1, the color is lightened by interpolating toward white (#FFFFFF).
     * If the factor is less than or equal to 1, the color is darkened by scaling its RGB components toward black (#000000).
     * The alpha channel remains unchanged.
     *
     * @param color  The input color to adjust, in ARGB format.
     * @param factor The adjustment factor. Use values > 1.0f to lighten (e.g., 1.11f for slight lightening)
     *               or values <= 1.0f to darken (e.g., 0.95f for slight darkening).
     * @return The adjusted color in ARGB format.
     */
    @ColorInt
    public static int adjustColorBrightness(@ColorInt int color, float factor) {
        final int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        if (factor > 1.0f) {
            // Lighten: Interpolate toward white (255).
            final float t = 1.0f - (1.0f / factor); // Interpolation parameter.
            red = Math.round(red + (255 - red) * t);
            green = Math.round(green + (255 - green) * t);
            blue = Math.round(blue + (255 - blue) * t);
        } else {
            // Darken or no change: Scale toward black.
            red = (int) (red * factor);
            green = (int) (green * factor);
            blue = (int) (blue * factor);
        }

        // Ensure values are within [0, 255].
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);

        return Color.argb(alpha, red, green, blue);
    }

    public static int clamp(int value, int lower, int upper) {
        return Math.max(lower, Math.min(value, upper));
    }

    public static float clamp(float value, float lower, float upper) {
        return Math.max(lower, Math.min(value, upper));
    }
}
