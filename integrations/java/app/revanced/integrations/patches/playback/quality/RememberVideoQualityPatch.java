package app.revanced.integrations.patches.playback.quality;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

public class RememberVideoQualityPatch {

    public static int selectedQuality1 = -2;
    private static Boolean newVideo = false;
    private static Boolean userChangedQuality = false;

    public static void changeDefaultQuality(int defaultQuality) {
        Context context = ReVancedUtils.getContext();
        if (isConnectedWifi(context)) {
            try {
                SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "wifi_quality", defaultQuality + "");
            } catch (Exception ex) {
                LogHelper.printException(RememberVideoQualityPatch.class, "Failed to change default WI-FI quality:" + ex);
                Toast.makeText(context, "Failed to change default WI-FI quality:", Toast.LENGTH_SHORT).show();
            }
            LogHelper.debug(RememberVideoQualityPatch.class, "Changing default Wi-Fi quality to: " + defaultQuality);
            Toast.makeText(context, "Changing default Wi-Fi quality to: " + defaultQuality, Toast.LENGTH_SHORT).show();
        } else if (isConnectedMobile(context)) {
            try {
                SharedPrefHelper.saveString(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "mobile_quality", defaultQuality + "");
            } catch (Exception ex) {
                LogHelper.debug(RememberVideoQualityPatch.class, "Failed to change default mobile data quality" + ex);
                Toast.makeText(context, "Failed to change default mobile data quality", Toast.LENGTH_SHORT).show();
            }
            LogHelper.debug(RememberVideoQualityPatch.class, "Changing default mobile data quality to:" + defaultQuality);
            Toast.makeText(context, "Changing default mobile data quality to:" + defaultQuality, Toast.LENGTH_SHORT).show();
        } else {
            LogHelper.debug(RememberVideoQualityPatch.class,  "No internet connection.");
            Toast.makeText(context, "No internet connection.", Toast.LENGTH_SHORT).show();
        }
        userChangedQuality = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface, String qIndexMethod) {
        int preferredQuality;
        Field[] fields;
        if (!(newVideo || userChangedQuality) || qInterface == null) {
            return quality;
        }
        Class<?> intType = Integer.TYPE;
        ArrayList<Integer> iStreamQualities = new ArrayList<>();
        try {
            for (Object streamQuality : qualities) {
                for (Field field : streamQuality.getClass().getFields()) {
                    if (field.getType().isAssignableFrom(intType)) {  // converts quality index to actual readable resolution
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
        if (userChangedQuality) {
            for (int convertedQuality : iStreamQualities) {
                int selectedQuality2 = qualities.length - selectedQuality1 + 1;
                index++;
                if (selectedQuality2 == index) {
                    LogHelper.debug(RememberVideoQualityPatch.class, "Quality index is: " + index + " and corresponding value is: " + convertedQuality);
                    changeDefaultQuality(convertedQuality);
                    return selectedQuality2;
                }
            }
        }
        newVideo = false;
        LogHelper.debug(RememberVideoQualityPatch.class, "Quality: " + quality);
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(RememberVideoQualityPatch.class, "Context is null or settings not initialized, returning quality: " + quality);
            return quality;
        }
        if (isConnectedWifi(context)) {
            preferredQuality = SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "wifi_quality", -2);
            LogHelper.debug(RememberVideoQualityPatch.class, "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (isConnectedMobile(context)) {
            preferredQuality = SharedPrefHelper.getInt(context, SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "mobile_quality", -2);
            LogHelper.debug(RememberVideoQualityPatch.class, "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.debug(RememberVideoQualityPatch.class, "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
        for (int streamQuality2 : iStreamQualities) {
            LogHelper.debug(RememberVideoQualityPatch.class, "Quality at index " + index + ": " + streamQuality2);
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
        LogHelper.debug(RememberVideoQualityPatch.class, "Index of quality " + quality + " is " + qualityIndex);
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod(qIndexMethod, Integer.TYPE);
            LogHelper.debug(RememberVideoQualityPatch.class, "Method is: " + qIndexMethod);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.debug(RememberVideoQualityPatch.class, "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(RememberVideoQualityPatch.class, "Failed to set quality", ex);
            Toast.makeText(context, "Failed to set quality", Toast.LENGTH_SHORT).show();
            return qualityIndex;
        }
    }

    public static void userChangedQuality(int selectedQuality) {
        // Do not remember a **new** quality if REMEMBER_VIDEO_QUALITY is true
        if (SettingsEnum.REMEMBER_VIDEO_QUALITY.getBoolean()) return;

        selectedQuality1 = selectedQuality;
        userChangedQuality = true;
    }

    public static void newVideoStarted(String videoId) {
        newVideo = true;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnected() && info.getType() == 1;
    }

    private static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnected() && info.getType() == 0;
    }

}
