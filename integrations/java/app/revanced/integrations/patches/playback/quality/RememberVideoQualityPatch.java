package app.revanced.integrations.patches.playback.quality;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;
import app.revanced.integrations.utils.SharedPrefHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

public class RememberVideoQualityPatch {

    public static int selectedQuality1 = -2;
    private static Boolean newVideo = false;
    private static Boolean userChangedQuality = false;

    public static void changeDefaultQuality(int defaultQuality) {
        Context context = ReVancedUtils.getContext();

        var networkType = getNetworType(context);

        if (networkType == NetworkType.NONE) {
            String message = "No internet connection.";
            LogHelper.printDebug(() -> message);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } else {
            var preferenceKey = "wifi_quality";
            var networkTypeMessage = "WIFI";

            if (networkType == NetworkType.MOBILE) {
                networkTypeMessage = "mobile";
                preferenceKey = "mobile_quality";
            }

            SharedPrefHelper.saveString(SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, preferenceKey, defaultQuality + "");
            String message = "Changing default" + networkTypeMessage + "quality to:" + defaultQuality;
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        userChangedQuality = false;
    }

    public static int setVideoQuality(Object[] qualities, int quality, Object qInterface, String qIndexMethod) {
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
        var networkType = getNetworType(context);
        if (networkType == NetworkType.NONE) {
            LogHelper.printDebug(() -> "No Internet connection!");
            return quality;
        } else {
            var preferenceKey = "wifi_quality";
            if (networkType == NetworkType.MOBILE) preferenceKey = "mobile_quality";

            int preferredQuality = SharedPrefHelper.getInt(SharedPrefHelper.SharedPrefNames.REVANCED_PREFS, preferenceKey, -2);
            if (preferredQuality == -2) return quality;

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
            if (quality == -2) return quality;

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
    }

    public static void userChangedQuality(int selectedQuality) {
        if (!SettingsEnum.REMEMBER_VIDEO_QUALITY_LAST_SELECTED.getBoolean()) return;

        selectedQuality1 = selectedQuality;
        userChangedQuality = true;
    }

    public static void newVideoStarted(String videoId) {
        newVideo = true;
    }

    private static NetworkType getNetworType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        var networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return NetworkType.NONE;
        } else {
            var type = networkInfo.getType();

            return type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_BLUETOOTH ? NetworkType.MOBILE : NetworkType.OTHER;
        }
    }

    enum NetworkType {
        MOBILE,
        OTHER,
        NONE
    }

}
