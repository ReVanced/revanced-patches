package app.revanced.integrations.patches;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class VideoQualityPatch {
    public static final int[] videoResolutions = {0, 144, 240, 360, 480, 720, 1080, 1440, 2160};
    private static Boolean userChangedQuality = false;
    public static int selectedQuality1 = -2;

    public static void changeDefaultQuality(int defaultQuality) {
        Context context = ReVancedUtils.getContext();
        if (isConnectedWifi(context)) {
            SharedPreferences wifi = context.getSharedPreferences("revanced_prefs", 0);
            SharedPreferences.Editor wifieditor = wifi.edit();
            wifieditor.putInt("wifi_quality",defaultQuality);
            wifieditor.apply();
            LogHelper.debug(VideoQualityPatch.class, "Changing default Wi-Fi quality to: " + defaultQuality);
        } else if (isConnectedMobile(context)) {
            SharedPreferences mobile = context.getSharedPreferences("revanced_prefs", 0);
            SharedPreferences.Editor mobileeditor = mobile.edit();
            mobileeditor.putInt("mobile_quality",defaultQuality);
            mobileeditor.apply();
            LogHelper.debug(VideoQualityPatch.class, "Changing default mobile data quality to: " + defaultQuality);
        } else {
            LogHelper.debug(VideoQualityPatch.class, "No Internet connection, aborting default quality change.");
        }
        userChangedQuality = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface, String qIndexMethod) {
        int preferredQuality;
        Field[] fields;
        if (!ReVancedUtils.isNewVideoStarted() && !userChangedQuality || qInterface == null) {
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
                    LogHelper.debug(VideoQualityPatch.class, "Quality index is: " + index + " and corresponding value is: " + convertedQuality);
                    changeDefaultQuality(convertedQuality);
                    return selectedQuality2;
                }
            }
        }
        ReVancedUtils.setNewVideo(false);
        LogHelper.debug(VideoQualityPatch.class, "Quality: " + quality);
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(VideoQualityPatch.class, "Context is null or settings not initialized, returning quality: " + quality);
            return quality;
        }
        if (isConnectedWifi(context)) {
            SharedPreferences wifi = context.getSharedPreferences("revanced_prefs", 0);
            preferredQuality = wifi.getInt("wifi_quality", -2);
            LogHelper.debug(VideoQualityPatch.class, "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (isConnectedMobile(context)) {
            SharedPreferences mobile = context.getSharedPreferences("revanced_prefs", 0);
            preferredQuality = mobile.getInt("mobile_quality", -2);
            LogHelper.debug(VideoQualityPatch.class, "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.debug(VideoQualityPatch.class, "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
        for (int streamQuality2 : iStreamQualities) {
            LogHelper.debug(VideoQualityPatch.class, "Quality at index " + index + ": " + streamQuality2);
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
        LogHelper.debug(VideoQualityPatch.class, "Index of quality " + quality + " is " + qualityIndex);
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod(qIndexMethod, Integer.TYPE);
            LogHelper.debug(VideoQualityPatch.class, "Method is: " + qIndexMethod);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.debug(VideoQualityPatch.class, "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(VideoQualityPatch.class, "Failed to set quality", ex);
            return qualityIndex;
        }
    }

    public static void userChangedQuality(int selectedQuality) {
        selectedQuality1 = selectedQuality;
        userChangedQuality = true;
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
