package fi.razerman.youtube.videosettings;

import android.content.Context;
import android.util.Log;
import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;
import fi.razerman.youtube.Connectivity;
import fi.razerman.youtube.XGlobals;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

/* loaded from: classes6.dex */
public class VideoQuality {
    static final int[] videoResolutions = {0, 144, 240, 360, 480, 720, 1080, 1440, 2160};

    public static void userChangedQuality() {
        XGlobals.userChangedQuality = true;
        XGlobals.newVideo = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface) {
        int preferredQuality;
        Field[] fields;
        if (!XGlobals.newVideo || XGlobals.userChangedQuality || qInterface == null) {
            if (XGlobals.debug && XGlobals.userChangedQuality) {
                Log.d("XGlobals - quality", "Skipping quality change because user changed it: " + quality);
            }
            XGlobals.userChangedQuality = false;
            return quality;
        }
        XGlobals.newVideo = false;
        if (XGlobals.debug) {
            Log.d("XGlobals - quality", "Quality: " + quality);
        }
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("XGlobals", "Context is null or settings not initialized, returning quality: " + quality);
            return quality;
        }
        if (Connectivity.isConnectedWifi(context)) {
            preferredQuality = XGlobals.prefResolutionWIFI;
            if (XGlobals.debug) {
                Log.d("XGlobals", "Wi-Fi connection detected, preferred quality: " + preferredQuality);
            }
        } else if (Connectivity.isConnectedMobile(context)) {
            preferredQuality = XGlobals.prefResolutionMobile;
            if (XGlobals.debug) {
                Log.d("XGlobals", "Mobile data connection detected, preferred quality: " + preferredQuality);
            }
        } else {
            if (XGlobals.debug) {
                Log.d("XGlobals", "No Internet connection!");
            }
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
        } catch (Exception ignored) {}
        Collections.sort(iStreamQualities);
        int index = 0;
        for (int streamQuality2 : iStreamQualities) {
            if (XGlobals.debug) {
                Log.d("XGlobals - qualities", "Quality at index " + index + ": " + streamQuality2);
            }
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
        if (XGlobals.debug) {
            Log.d("XGlobals", "Index of quality " + quality + " is " + qualityIndex);
        }
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod("x", Integer.TYPE);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            if (XGlobals.debug) {
                Log.d("XGlobals", "Quality changed to: " + qualityIndex);
            }
            return qualityIndex;
        } catch (Exception ex) {
            Log.e("XGlobals", "Failed to set quality", ex);
            return qualityIndex;
        }
    }
}
