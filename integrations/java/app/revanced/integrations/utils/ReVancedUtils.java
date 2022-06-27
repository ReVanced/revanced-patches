package app.revanced.integrations.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import app.revanced.integrations.sponsorblock.player.PlayerType;

public class ReVancedUtils {

    private static PlayerType env;

    //Used by Integrations patch
    public static Context context;
    //Used by Integrations patch
    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException("Globals", "Context is null!");
        return null;
    }

    public static String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            LogHelper.printException("Settings", "Resource not found.", exception);
            return "";
        }
    }

    public static void setPlayerType(PlayerType type) {
        env = type;
    }

    public static PlayerType getPlayerType() {
        return env;
    }

    public static int getIdentifier(String name, String defType) {
        Context context = getContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void CheckForMicroG(Activity activity) {
        AlertDialog.Builder builder;
        if (!appInstalledOrNot("com.mgoogle.android.gms")) {
            LogHelper.debug("XDebug", "Custom MicroG installation undetected");
            if (Build.VERSION.SDK_INT >= 21) {
                builder = new AlertDialog.Builder(activity, 16974374);
            } else {
                builder = new AlertDialog.Builder(activity);
            }
            builder.setTitle("Someone is not reading...").setMessage("You didn't install the MicroG as instructed, you can't login without it.\n\nInstall it and try again.").setPositiveButton("Close", new DialogInterface.OnClickListener() { // from class: app.revanced.integrations.settings.Settings.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                }
            }).show();
        } else {
            LogHelper.debug("XDebug", "Custom MicroG installation detected");
        }
    }

    private static boolean appInstalledOrNot(String uri) {
        try {
            PackageManager pm = getContext().getPackageManager();
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Context getContext() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context != null) {
            return context;
        } else {
            LogHelper.printException("Settings", "Context is null, returning null!");
            return null;
        }
    }

    //Not used atm
    /*public static String getManufacturer() {
        String manufacturer = SettingsEnum.MANUFACTURER_OVERRIDE_STRING.getString();
        if (manufacturer == null || manufacturer.isEmpty()) {
            manufacturer = Build.MANUFACTURER;
        }
        LogHelper.debug("Settings", "getManufacturer: " + manufacturer);
        return manufacturer;
    }

    public static String getModel() {
        String model = SettingsEnum.MODEL_OVERRIDE_STRING.getString();
        if (model == null || model.isEmpty()) {
            model = Build.MODEL;
        }
        LogHelper.debug("Settings", "getModel: " + model);
        return model;
    }*/
}