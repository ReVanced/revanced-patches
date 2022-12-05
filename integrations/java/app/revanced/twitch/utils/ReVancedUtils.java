package app.revanced.twitch.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ReVancedUtils {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    /**
     * Regular context getter
     * @return Returns context or null if not initialized
     */
    public static Context getContext() {
        if (context != null) {
            return context;
        }

        LogHelper.error("Context is null (at %s)", LogHelper.getCallOrigin());
        return null;
    }

    /**
     * Execute lambda only if context attached.
     */
    public static void ifContextAttached(SafeContextAccessLambda lambda) {
        if (context != null) {
            lambda.run(context);
            return;
        }

        LogHelper.error("Context is null, lambda not executed (at %s)", LogHelper.getCallOrigin());
    }

    /**
     * Execute lambda only if context attached.
     * @return Returns result on success or valueOnError on failure
     */
    public static <T> T ifContextAttached(SafeContextAccessReturnLambda<T> lambda, T valueOnError) {
        if (context != null) {
            return lambda.run(context);
        }

        LogHelper.error("Context is null, lambda not executed (at %s)", LogHelper.getCallOrigin());
        return valueOnError;
    }

    public interface SafeContextAccessReturnLambda<T> {
        T run(Context ctx);
    }

    public interface SafeContextAccessLambda {
        void run(Context ctx);
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * Get resource id safely
     * @return May return 0 if resource not found or context not attached
     */
    @SuppressLint("DiscouragedApi")
    public static int getIdentifier(String name, String defType) {
        return ifContextAttached(
                (context) -> {
                    int resId = context.getResources().getIdentifier(name, defType, context.getPackageName());
                    if(resId == 0) {
                        LogHelper.error("Resource '%s' not found (at %s)", name, LogHelper.getCallOrigin());
                    }
                    return resId;
                },
                0
        );
    }

    /* Called from SettingsPatch smali */
    public static int getStringId(String name) {
        return getIdentifier(name, "string");
    }

    /* Called from SettingsPatch smali */
    public static int getDrawableId(String name) {
        return getIdentifier(name, "drawable");
    }

    public static String getString(String name) {
        return ifContextAttached((c) -> c.getString(getStringId(name)), "");
    }

    public static void toast(String message) {
        toast(message, true);
    }
    public static void toast(String message, boolean longLength) {
        ifContextAttached((c) -> {
            runOnMainThread(() -> Toast.makeText(c, message, longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show());
        });
    }
}
