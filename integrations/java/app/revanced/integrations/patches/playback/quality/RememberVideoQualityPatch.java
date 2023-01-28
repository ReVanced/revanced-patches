package app.revanced.integrations.patches.playback.quality;

import android.annotation.SuppressLint;
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
                SharedPrefHelper.saveString(SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "wifi_quality", defaultQuality + "");
                String message = "Changing default Wi-Fi quality to: " + defaultQuality;
                LogHelper.printDebug(() -> message);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                LogHelper.printException(() -> "Failed to change default WI-FI quality", ex);
            }
        } else if (isConnectedMobile(context)) {
            try {
                SharedPrefHelper.saveString(SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "mobile_quality", defaultQuality + "");
                String message = "Changing default mobile data quality to:" + defaultQuality;
                LogHelper.printDebug(() -> message);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                LogHelper.printException(() -> "Failed to change default mobile data quality", ex);
            }
        } else {
            String message = "No internet connection.";
            LogHelper.printDebug(() -> message);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
                    final int indexToLog = index; // must be final for lambda
                    LogHelper.printDebug(() -> "Quality index is: " + indexToLog + " and corresponding value is: " + convertedQuality);
                    changeDefaultQuality(convertedQuality);
                    return selectedQuality2;
                }
            }
        }
        newVideo = false;
        final int qualityToLog = quality;
        LogHelper.printDebug(() -> "Quality: " + qualityToLog);
        Context context = ReVancedUtils.getContext();
        if (context == null) {
            LogHelper.printException(() -> "Context is null or settings not initialized, returning quality: " + qualityToLog);
            return quality;
        }
        if (isConnectedWifi(context)) {
            preferredQuality = SharedPrefHelper.getInt(SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "wifi_quality", -2);
            LogHelper.printDebug(() -> "Wi-Fi connection detected, preferred quality: " + preferredQuality);
        } else if (isConnectedMobile(context)) {
            preferredQuality = SharedPrefHelper.getInt(SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, "mobile_quality", -2);
            LogHelper.printDebug(() -> "Mobile data connection detected, preferred quality: " + preferredQuality);
        } else {
            LogHelper.printDebug(() -> "No Internet connection!");
            return quality;
        }
        if (preferredQuality == -2) {
            return quality;
        }
        for (int streamQuality2 : iStreamQualities) {
            final int indexToLog = index;
            LogHelper.printDebug(() -> "Quality at index " + indexToLog + ": " + streamQuality2);
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
        final int qualityToLog2 = quality;
        LogHelper.printDebug(() -> "Index of quality " + qualityToLog2 + " is " + qualityIndex);
        try {
            Class<?> cl = qInterface.getClass();
            Method m = cl.getMethod(qIndexMethod, Integer.TYPE);
            LogHelper.printDebug(() -> "Method is: " + qIndexMethod);
            m.invoke(qInterface, iStreamQualities.get(qualityIndex));
            LogHelper.printDebug(() -> "Quality changed to: " + qualityIndex);
            return qualityIndex;
        } catch (Exception ex) {
            LogHelper.printException(() -> "Failed to set quality", ex);
            return qualityIndex;
        }
    }

    public static void userChangedQuality(int selectedQuality) {
        // Do not remember a **new** quality if REMEMBER_VIDEO_QUALITY is false
        if (!SettingsEnum.REMEMBER_VIDEO_QUALITY_LAST_SELECTED.getBoolean()) return;

        selectedQuality1 = selectedQuality;
        userChangedQuality = true;
    }

    public static void newVideoStarted(String videoId) {
        newVideo = true;
    }

    @SuppressLint("MissingPermission")
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
