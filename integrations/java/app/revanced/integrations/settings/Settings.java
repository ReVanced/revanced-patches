package app.revanced.integrations.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import app.revanced.integrations.sponsorblock.player.PlayerType;
import app.revanced.integrations.sponsorblock.SponsorBlockUtils;
import app.revanced.integrations.sponsorblock.player.ui.SponsorBlockView;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.SharedPrefHelper;
import app.revanced.integrations.videoswipecontrols.FensterGestureController;
import app.revanced.integrations.videoswipecontrols.XFenster;
import app.revanced.integrations.utils.SwipeHelper;
import app.revanced.integrations.sponsorblock.NewSegmentHelperLayout;

public class Settings {
    private static Object AutoRepeatClass;
    private static PlayerType env;
    private static FensterGestureController fensterGestureController;

    public static Boolean userChangedQuality = false;
    public static Boolean userChangedSpeed = false;
    public static Boolean newVideo = false;
    public static Boolean newVideoSpeed = false;
    public static float[] videoSpeeds = { 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2f, 3f, 4f, 5f };

    public static String getManufacturer() {
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

    public static boolean isFensterEnabled() {
        if (env != null && env == PlayerType.WATCH_WHILE_FULLSCREEN && !SwipeHelper.IsControlsShown()) {
            return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() || SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean();
        }
        return false;
    }

    public static boolean isWatchWhileFullScreen() {
        if (env == null) {
            return false;
        }
        return env.toString().equals("WATCH_WHILE_FULLSCREEN");
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

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return (version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ("17.23.35");
    }

    public static String getPackageName() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            LogHelper.printException("Settings", "Context is null, returning com.google.android.youtube!");
            return "com.google.android.youtube";
        }
        String PACKAGE_NAME = context.getPackageName();
        LogHelper.debug("Settings", "getPackageName: " + PACKAGE_NAME);

        return PACKAGE_NAME;
    }

    public static int getOverrideWidth(int original) {
        int compatibility = SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getBoolean() ? 2160 : original;
        return compatibility;
    }

    public static int getOverrideHeight(int original) {
        int compatibility = SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getBoolean() ? 3840 : original;
        return compatibility;
    }

    public static Context getContext() {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context != null) {
            LogHelper.debug("Settings", "getContext");
            return context;
        } else {
            LogHelper.printException("Settings", "Context is null, returning null!");
            return null;
        }
    }

    public static void setOldLayout(SharedPreferences sharedPreferences, String config, long timeStamp) {
        if (!SettingsEnum.OLD_LAYOUT_XFILE_ENABLED_BOOLEAN.getBoolean()) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group", config).putLong("com.google.android.libraries.youtube.innertube.cold_stored_timestamp", timeStamp).apply();
            LogHelper.debug("Settings", "setOldLayout: true");
            return;
        }

        if (sharedPreferences.contains("com.google.android.libraries.youtube.innertube.cold_config_group")) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group_backup", sharedPreferences.getString("com.google.android.libraries.youtube.innertube.cold_config_group", null)).remove("com.google.android.libraries.youtube.innertube.cold_config_group").apply();
        }
        LogHelper.debug("Settings", "setOldLayout: false");
    }

    public static void NewVideoStarted() {
        newVideo = true;
        newVideoSpeed = true;
        LogHelper.debug("Settings", "New video started!");
    }

    public static void InitializeFensterController(Context context, ViewGroup viewGroup, ViewConfiguration viewConfiguration) {
        fensterGestureController = new FensterGestureController();
        fensterGestureController.setFensterEventsListener(new XFenster(context, viewGroup), context, viewConfiguration);
        LogHelper.debug("Settings", "XFenster initialized");
    }

    public static boolean FensterTouchEvent(MotionEvent motionEvent) {
        if (fensterGestureController == null) {
            LogHelper.debug("Settings", "fensterGestureController is null");
            return false;
        } else if (motionEvent == null) {
            LogHelper.debug("Settings", "motionEvent is null");
            return false;
        } else if (!SwipeHelper.IsControlsShown()) {
            return fensterGestureController.onTouchEvent(motionEvent);
        } else {
            LogHelper.debug("Settings", "skipping onTouchEvent dispatching because controls are shown.");
            return false;
        }
    }

    public static void PlayerTypeChanged(PlayerType playerType) {
        LogHelper.debug("XDebug", playerType.toString());
        if (env != playerType) {
            if (playerType == PlayerType.WATCH_WHILE_FULLSCREEN) {
                EnableXFenster();
            } else {
                DisableXFenster();
            }
            if (playerType == PlayerType.WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || playerType == PlayerType.WATCH_WHILE_MINIMIZED || playerType == PlayerType.WATCH_WHILE_PICTURE_IN_PICTURE) {
                NewSegmentHelperLayout.hide();
            }
            SponsorBlockView.playerTypeChanged(playerType);
            SponsorBlockUtils.playerTypeChanged(playerType);
        }
        env = playerType;
    }

    public static void EnableXFenster() {
        if (SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() || SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean()) {
            FensterGestureController fensterGestureController2 = fensterGestureController;
            fensterGestureController2.TouchesEnabled = true;
            ((XFenster) fensterGestureController2.listener).enable(SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean(), SettingsEnum.ENABLE_SWIPE_VOLUME_BOOLEAN.getBoolean());
        }
    }

    public static void DisableXFenster() {
        FensterGestureController fensterGestureController2 = fensterGestureController;
        fensterGestureController2.TouchesEnabled = false;
        ((XFenster) fensterGestureController2.listener).disable();
    }

    /*public static boolean autoCaptions(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean captions = Boolean.valueOf(original);
        if (prefAutoCaptions.booleanValue()) {
            captions = true;
        }
        LogHelper.debug("Settings", "autoCaptions: " + captions);
        return captions.booleanValue();
    }

    public static boolean getOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean compatibility = Boolean.valueOf(original);
        if (overrideCodec.booleanValue()) {
            compatibility = true;
        }
        LogHelper.debug("Settings", "getOverride: " + compatibility);

        return compatibility.booleanValue();
    }

    public static int getCommentsLocation(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!commentsLocation.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getCommentsLocation: Moving comments back down");
            return 3;
        }
    }

    public static boolean getTabletMiniplayerOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!tabletMiniplayer.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getTabletMiniplayerOverride: Using tablet miniplayer");
            return true;
        }
    }

    public static boolean getNewActionBar(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!newActionBar.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getNewActionBar: Enabled");
            return true;
        }
    }*/

    /*
    public static boolean getNewActionBarNegated(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!newActionBar.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getNewActionBar: Enabled");
            return false;
        }
    }

    public static boolean getVerticalZoomToFit(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!verticalZoomToFit.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getVerticalZoomToFit: Enabled");
            return true;
        }
    }

    public static int getMinimizedVideo(int original) {
        int preferredType = SettingsEnum.PREFERRED_MINIMIZED_VIDEO_PREVIEW_INTEGER.getInt();
        if (preferredType == -2) {
            return original;
        }
        if (preferredType == 0 || preferredType == 1) {
            return preferredType;
        }
        return original;
    }


    public static boolean getThemeStatus() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning false!");
            return false;
        } else if (!isDarkApp.booleanValue()) {
            return false;
        } else {
            LogHelper.debug("Settings", "getThemeStatus: Is themed");
            return true;
        }
    }

    public static boolean accessibilitySeek(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean seek = Boolean.valueOf(original);
        if (accessibilitySeek.booleanValue()) {
            seek = true;
        }
        LogHelper.debug("Settings", "accessibilitySeek: " + seek);
        return seek.booleanValue();
    }

    public static boolean shouldAutoRepeat() {
        return SettingsEnum.PREFERRED_AUTO_REPEAT_BOOLEAN.getBoolean();
    }

    public static float getHDRBrightness(float original) {
        if (!SettingsEnum.USE_HDR_BRIGHTNESS_BOOLEAN.getBoolean()) return original;
        return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean() ? BrightnessHelper.getBrightness() : -1.0f;
    }

    public static int getMaxBuffer(int original) {
        return SettingsEnum.MAX_BUFFER_INTEGER.getInt();
    }



    public static int getPlaybackBuffer(int original) {
        return SettingsEnum.PLAYBACK_MAX_BUFFER_INTEGER.getInt();
    }
    */

/*
    public static int getReBuffer(int original) {
        return SettingsEnum.MAX_PLAYBACK_BUFFER_AFTER_REBUFFER_INTEGER.getInt();
    }

    public static boolean isFensterBrightnessEnabled() {
        return SettingsEnum.ENABLE_SWIPE_BRIGHTNESS_BOOLEAN.getBoolean();
    }
    */


}
