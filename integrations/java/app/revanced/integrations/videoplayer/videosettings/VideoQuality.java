package app.revanced.integrations.videoplayer.videosettings;

import android.content.Context;


import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.settings.Settings;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/* loaded from: classes6.dex */
public class VideoQuality {
    static final int[] videoResolutions = {0, 144, 240, 360, 480, 720, 1080, 1440, 2160};

    public static void userChangedQuality() {
        Settings.userChangedQuality = true;
        Settings.newVideo = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface) {
        int preferredQuality;
        Field[] fields;
        if (!Settings.newVideo || Settings.userChangedQuality || qInterface == null) {
            if (SettingsEnum.DEBUG_BOOLEAN.getBoolean() && Settings.userChangedQuality) {
                LogHelper.debug("Settings - quality", "Skipping quality change because user changed it: " + quality);
            }
            Settings.userChangedQuality = false;
            return quality;
        }
        Settings.newVideo = false;
        LogHelper.debug("Settings - quality", "Quality: " + quality);
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("Settings", "Context is null or settings not initialized, returning quality: " + quality);
            return quality;
        }
        if (Connectivity.isConnectedWifi(context)) {
            preferredQuality = SettingsEnum.PREFERRED_RESOLUTION_WIFI_INTEGER.getInt();
            LogHelper.debug("Settings", "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (Connectivity.isConnectedMobile(context)) {
            preferredQuality = SettingsEnum.PREFERRED_RESOLUTION_MOBILE_INTEGER.getInt();
            LogHelper.debug("Settings", "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.debug("Settings", "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
        Class<?> intType = Integer.TYPE;
        ArrayList<Integer> iStreamQualities = new ArrayList<>();
        try {
            for (Object streamQuality : qualities) {
                for (Field field : streamQuality.getClass().getFields()) {
                    if (field.getType().isAssignableFrom(intType)) {
                        int value = field.getInt(streamQuality);
                        if (field.getName().length() <= 2) {
                            iStreamQualities.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Collections.sort(iStreamQualities);
        int index = 0;
        for (int streamQuality2 : iStreamQualities) {
            LogHelper.debug("Settings - qualities", "Quality at index " + index + ": " + streamQuality2);
            index++;
        }
        for (Integer iStreamQuality : iStreamQualities) {
            int streamQuality3 = iStreamQuality;
            if (streamQuality3 <= preferredQuality) {
                quality = streamQuality3;
            }
        }
        if (quality == -2) {
            return quality;
        }
        int qualityIndex = iStreamQualities.indexOf(quality);
        LogHelper.debug("Settings", "Index of quality " + quality + " is " + qualityIndex);
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod("x", Integer.TYPE);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.debug("Settings", "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException("Settings", "Failed to set quality", ex);
            return qualityIndex;
        }
    }
}
