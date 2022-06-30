package app.revanced.integrations.videoswipecontrols.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;

import android.view.WindowManager;

import app.revanced.integrations.utils.LogHelper;

/* loaded from: classes6.dex */
public class BrightnessHelper {
    static Context bContext;

    public static float getBrightness() {
        Context context = bContext;
        if (context == null) {
            return -1.0f;
        }
        return ((Activity) context).getWindow().getAttributes().screenBrightness;
    }

    public static int getBrightness(Context context) {
        bContext = context;
        return (int) (((Activity) context).getWindow().getAttributes().screenBrightness * 100.0f);
    }

    public static void setBrightness(Context context, int brightness) {
        LogHelper.debug(BrightnessHelper.class, "Setting brightness: " + brightness);
        float bright = brightness / 100.0f;
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.screenBrightness = bright;
        ((Activity) context).getWindow().setAttributes(lp);
    }

    public static void setBrightness2(Context context, int brightness) {
        LogHelper.debug(BrightnessHelper.class, "Setting brightness: " + brightness);
        ContentResolver cResolver = context.getContentResolver();
        android.provider.Settings.System.putInt(cResolver, "screen_brightness", brightness);
    }

    public static int getBrightness2(Context context) {
        ContentResolver cResolver = context.getContentResolver();
        try {
            return android.provider.Settings.System.getInt(cResolver, "screen_brightness");
        } catch (android.provider.Settings.SettingNotFoundException e) {
            return 0;
        }
    }
}
