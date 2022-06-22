package fi.razerman.youtube;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;

import fi.razerman.youtube.Fenster.FensterGestureController;
import fi.razerman.youtube.Fenster.FensterGestureListener;
import fi.razerman.youtube.Fenster.Helpers.BrightnessHelper;
import fi.razerman.youtube.Fenster.XFenster;
import fi.razerman.youtube.Helpers.ColorRef;
import fi.razerman.youtube.Helpers.XSwipeHelper;
import pl.jakubweg.NewSegmentHelperLayout;

/* compiled from: PG */
/* renamed from: env */
/* loaded from: classes3.dex */
enum env {
    NONE,
    HIDDEN,
    WATCH_WHILE_MINIMIZED,
    WATCH_WHILE_MAXIMIZED,
    WATCH_WHILE_FULLSCREEN,
    WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN,
    WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED,
    WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED,
    WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED,
    INLINE_MINIMAL,
    VIRTUAL_REALITY_FULLSCREEN,
    WATCH_WHILE_PICTURE_IN_PICTURE;

    /* renamed from: a */
    public final boolean m33524a() {
        return !m33520e() && m33523b() && m33517h();
    }

    /* renamed from: b */
    public final boolean m33523b() {
        return this == WATCH_WHILE_FULLSCREEN || this == VIRTUAL_REALITY_FULLSCREEN || this == WATCH_WHILE_PICTURE_IN_PICTURE;
    }

    /* renamed from: c */
    public final boolean m33522c() {
        return this == NONE || m33521d();
    }

    /* renamed from: d */
    public final boolean m33521d() {
        return this == INLINE_MINIMAL;
    }

    /* renamed from: e */
    public final boolean m33520e() {
        return this == WATCH_WHILE_PICTURE_IN_PICTURE;
    }

    /* renamed from: f */
    public final boolean m33519f() {
        return (this == NONE || this == HIDDEN) ? false : true;
    }

    /* renamed from: g */
    public final boolean m33518g() {
        return this == VIRTUAL_REALITY_FULLSCREEN;
    }

    /* renamed from: h */
    public final boolean m33517h() {
        return this == WATCH_WHILE_MINIMIZED || this == WATCH_WHILE_MAXIMIZED || this == WATCH_WHILE_FULLSCREEN || this == WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN || this == WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED || this == WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED || this == WATCH_WHILE_PICTURE_IN_PICTURE;
    }

    /* renamed from: i */
    public final boolean m33516i() {
        return this == WATCH_WHILE_MAXIMIZED || this == WATCH_WHILE_FULLSCREEN;
    }

    /* renamed from: j */
    public final boolean m33515j() {
        return m33516i() || this == WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN;
    }

    /* renamed from: k */
    public final boolean m33514k() {
        return this == WATCH_WHILE_MINIMIZED || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED;
    }

    /* renamed from: l */
    public final boolean m33513l() {
        return m33514k() || m33512m();
    }

    /* renamed from: m */
    public final boolean m33512m() {
        return this == WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED || this == WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN || this == WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED;
    }
}

/* loaded from: classes6.dex */
public class XGlobals {
    private static Object AutoRepeatClass;
    private static env PlayerType;
    public static FensterGestureController fensterGestureController;
    public static Boolean XFILEDEBUG = false;
    public static Boolean newVideo = false;
    public static Boolean newVideoSpeed = false;
    public static Boolean debug = false;
    private static Boolean settingsInitialized = false;
    public static String manufacturerOverride = null;
    public static String modelOverride = null;
    public static Boolean overrideCodec = false;
    public static Boolean userChangedQuality = false;
    public static Boolean userChangedSpeed = false;
    public static Integer prefResolutionWIFI = -2;
    public static Integer prefResolutionMobile = -2;
    public static Float prefVideoSpeed = -2.0f;
    public static Boolean prefAutoCaptions = false;
    public static Boolean homeAdsShown = false;
    public static Boolean videoAdsShown = false;
    public static Boolean reelShown = false;
    public static Boolean suggestionsShown = true;
    public static Boolean infoCardsShown = true;
    public static Boolean brandingShown = true;
    public static Boolean castButtonShown = false;
    public static Boolean tabletMiniplayer = false;
    public static Boolean commentsLocation = false;
    public static Boolean newActionBar = false;
    public static Boolean verticalZoomToFit = false;
    public static Boolean isDarkApp = false;
    public static Boolean accessibilitySeek = false;
    public static Boolean HDRBrightness = true;
    public static Boolean EnableXFensterBrightness = false;
    public static Boolean EnableXFensterVolume = false;
    public static Integer maxBuffer = 120000;
    public static Integer playbackMS = 2500;
    public static Integer reBuffer = 5000;
    public static Enum lastPivotTab;
    public static float[] videoSpeeds = { 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2f, 3f, 4f, 5f };

    public static void ReadSettings() {
        Context context;
        if (!settingsInitialized.booleanValue() && (context = YouTubeTikTokRoot_Application.getAppContext()) != null) {
            ColorRef.setContext(context);
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            debug = Boolean.valueOf(sharedPreferences.getBoolean("debug_xfile_enabled", false));
            manufacturerOverride = sharedPreferences.getString("override_manufacturer", null);
            modelOverride = sharedPreferences.getString("override_model", null);
            overrideCodec = sharedPreferences.getBoolean("override_resolution_xfile_enabled", false);
            prefResolutionWIFI = Integer.parseInt(sharedPreferences.getString("pref_preferred_video_quality_wifi", "-2"));
            prefResolutionMobile = Integer.valueOf(Integer.parseInt(sharedPreferences.getString("pref_preferred_video_quality_mobile", "-2")));
            prefVideoSpeed = Float.valueOf(Float.parseFloat(sharedPreferences.getString("pref_preferred_video_speed", "-2")));
            prefAutoCaptions = Boolean.valueOf(sharedPreferences.getBoolean("pref_auto_captions", false));
            homeAdsShown = Boolean.valueOf(sharedPreferences.getBoolean("home_ads_enabled", false));
            videoAdsShown = Boolean.valueOf(sharedPreferences.getBoolean("video_ads_enabled", false));
            reelShown = Boolean.valueOf(sharedPreferences.getBoolean("reel_enabled", false));
            suggestionsShown = Boolean.valueOf(sharedPreferences.getBoolean("info_card_suggestions_enabled", true));
            infoCardsShown = Boolean.valueOf(sharedPreferences.getBoolean("info_cards_enabled", true));
            brandingShown = Boolean.valueOf(sharedPreferences.getBoolean("branding_watermark_enabled", true));
            castButtonShown = Boolean.valueOf(sharedPreferences.getBoolean("cast_button_enabled", false));
            tabletMiniplayer = Boolean.valueOf(sharedPreferences.getBoolean("tablet_miniplayer", false));
            commentsLocation = Boolean.valueOf(sharedPreferences.getBoolean("comments_location", false));
            newActionBar = Boolean.valueOf(sharedPreferences.getBoolean("xfile_new_actionbar", false));
            verticalZoomToFit = Boolean.valueOf(sharedPreferences.getBoolean("xfile_zoom_to_fit_vertical", false));
            isDarkApp = Boolean.valueOf(sharedPreferences.getBoolean("app_theme_dark", false));
            accessibilitySeek = Boolean.valueOf(sharedPreferences.getBoolean("xfile_accessibility_seek_buttons", false));
            HDRBrightness = Boolean.valueOf(sharedPreferences.getBoolean("pref_hdr_autobrightness", true));
            if (sharedPreferences.getBoolean("pref_xfenster", false)) {
                sharedPreferences.edit().remove("pref_xfenster").putBoolean("pref_xfenster_brightness", true).putBoolean("pref_xfenster_volume", true).apply();
            }
            EnableXFensterBrightness = Boolean.valueOf(sharedPreferences.getBoolean("pref_xfenster_brightness", false));
            EnableXFensterVolume = Boolean.valueOf(sharedPreferences.getBoolean("pref_xfenster_volume", false));
            try {
                FensterGestureListener.SWIPE_THRESHOLD = Integer.parseInt(sharedPreferences.getString("pref_xfenster_swipe_threshold", "0"));
            } catch (NumberFormatException e) {
                sharedPreferences.edit().putString("pref_xfenster_swipe_threshold", "0").apply();
                FensterGestureListener.SWIPE_THRESHOLD = 0;
            }
            try {
                FensterGestureListener.TOP_PADDING = Integer.parseInt(sharedPreferences.getString("pref_xfenster_swipe_padding_top", "20"));
            } catch (NumberFormatException e2) {
                sharedPreferences.edit().putString("pref_xfenster_swipe_padding_top", "20").apply();
                FensterGestureListener.TOP_PADDING = 20;
            }
            String string = sharedPreferences.getString("pref_max_buffer_ms", "120000");
            if (string.isEmpty()) {
                string = "1";
            }
            maxBuffer = Integer.valueOf(Integer.parseInt(string));
            String string2 = sharedPreferences.getString("pref_buffer_for_playback_ms", "2500");
            if (string2.isEmpty()) {
                string2 = "1";
            }
            playbackMS = Integer.valueOf(Integer.parseInt(string2));
            String string3 = sharedPreferences.getString("pref_buffer_for_playback_after_rebuffer_ms", "5000");
            if (string3.isEmpty()) {
                string3 = "1";
            }
            reBuffer = Integer.valueOf(Integer.parseInt(string3));
            settingsInitialized = true;
        }
    }

    public static String getManufacturer() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning Build.MANUFACTURER!");
            return Build.MANUFACTURER;
        }
        String manufacturer = manufacturerOverride;
        if (manufacturer == null || manufacturer.isEmpty()) {
            manufacturer = Build.MANUFACTURER;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getManufacturer: " + manufacturer);
        }
        return manufacturer;
    }

    public static String getModel() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning Build.MODEL!");
            return Build.MODEL;
        }
        String model = modelOverride;
        if (model == null || model.isEmpty()) {
            model = Build.MODEL;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getModel: " + model);
        }
        return model;
    }

    public static boolean autoCaptions(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean captions = Boolean.valueOf(original);
        if (prefAutoCaptions.booleanValue()) {
            captions = true;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "autoCaptions: " + captions);
        }
        return captions.booleanValue();
    }

    public static boolean getOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean compatibility = Boolean.valueOf(original);
        if (overrideCodec.booleanValue()) {
            compatibility = true;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getOverride: " + compatibility);
        }
        return compatibility.booleanValue();
    }

    public static int getCommentsLocation(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (!commentsLocation.booleanValue()) {
            return original;
        } else {
            if (!debug.booleanValue()) {
                return 3;
            }
            Log.d("XGlobals", "getCommentsLocation: Moving comments back down");
            return 3;
        }
    }

    public static boolean getTabletMiniplayerOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (!tabletMiniplayer.booleanValue()) {
            return original;
        } else {
            if (!debug.booleanValue()) {
                return true;
            }
            Log.d("XGlobals", "getTabletMiniplayerOverride: Using tablet miniplayer");
            return true;
        }
    }

    public static boolean getCastButtonOverride(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (castButtonShown.booleanValue()) {
            return original;
        } else {
            if (!debug.booleanValue()) {
                return true;
            }
            Log.d("XGlobals", "getCastButtonOverride: Hidden by override");
            return true;
        }
    }

    public static boolean getNewActionBar(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (!newActionBar.booleanValue()) {
            return original;
        } else {
            if (!debug.booleanValue()) {
                return true;
            }
            Log.d("XGlobals", "getNewActionBar: Enabled");
            return true;
        }
    }

    public static int getCastButtonOverrideV2(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (castButtonShown.booleanValue()) {
            return original;
        } else {
            if (debug.booleanValue()) {
                Log.d("XGlobals", "getCastButtonOverrideV2: Hidden by override");
            }
            return 8;
        }
    }

    public static boolean getNewActionBarNegated(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (!newActionBar.booleanValue()) {
            return original;
        } else {
            if (!debug.booleanValue()) {
                return false;
            }
            Log.d("XGlobals", "getNewActionBar: Enabled");
            return false;
        }
    }

    public static boolean getVerticalZoomToFit(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        } else if (!verticalZoomToFit.booleanValue()) {
            return original;
        } else {
            if (!debug.booleanValue()) {
                return true;
            }
            Log.d("XGlobals", "getVerticalZoomToFit: Enabled");
            return true;
        }
    }

    public static int getMinimizedVideo(int original) {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        int preferredType = Integer.parseInt(sharedPreferences.getString("pref_minimized_video_preview", "-2"));
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
            Log.e("XGlobals", "Context is null, returning false!");
            return false;
        } else if (!isDarkApp.booleanValue()) {
            return false;
        } else {
            if (!debug.booleanValue()) {
                return true;
            }
            Log.d("XGlobals", "getThemeStatus: Is themed");
            return true;
        }
    }

    public static boolean accessibilitySeek(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean seek = Boolean.valueOf(original);
        if (accessibilitySeek.booleanValue()) {
            seek = true;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "accessibilitySeek: " + seek);
        }
        return seek.booleanValue();
    }

    public static boolean useOldStyleQualitySettings() {
        boolean value;
        try {
            Context context = YouTubeTikTokRoot_Application.getAppContext();
            if (context == null) {
                Log.e("XGlobals", "useOldStyleQualitySettings - Context is null, returning false!");
                value = true;
            } else {
                SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
                value = sharedPreferences.getBoolean("old_style_quality_settings", true);
                if (debug.booleanValue()) {
                    Log.d("XGlobals", "old_style_quality_settings set to: " + value);
                }
            }
            return value;
        } catch (Exception ex) {
            Log.e("XGlobals", "Unable to get old style quality settings", ex);
            return true;
        }
    }

    public static boolean shouldAutoRepeat() {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("XGlobals", "shouldAutoRepeat - Context is null, returning false!");
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        boolean repeat = sharedPreferences.getBoolean("pref_auto_repeat", false);
        if (debug.booleanValue()) {
            Log.d("XGlobals", "shouldAutoRepeat: " + repeat);
        }
        return repeat;
    }

    @Deprecated
    public static void trySetAutonav(boolean autoNav) {
        try {
            ReadSettings();
            Context context = YouTubeTikTokRoot_Application.getAppContext();
            if (context == null) {
                Log.e("XGlobals", "shouldAutoRepeat - Context is null, returning false!");
                return;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            sharedPreferences.edit().putBoolean("autonav_settings_activity_key", autoNav).apply();
            if (debug.booleanValue()) {
                Log.d("XGlobals", "autonav_settings_activity_key set to: " + autoNav);
            }
        } catch (Exception e) {
        }
    }

    public static float getHDRBrightness(float original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, getHDRBrightness returning " + original + "!");
            return original;
        }
        float finalValue = original;
        if (!HDRBrightness.booleanValue()) {
            if (isFensterBrightnessEnabled()) {
                finalValue = BrightnessHelper.getBrightness();
            } else {
                finalValue = -1.0f;
            }
            if (debug.booleanValue()) {
                Log.d("XGlobals", "getHDRBrightness switched to: " + finalValue);
            }
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getHDRBrightness: " + finalValue);
        }
        return finalValue;
    }

    public static int getMaxBuffer(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, getMaxBuffer returning " + original + "!");
            return original;
        }
        int retrievedValue = maxBuffer.intValue();
        return retrievedValue;
    }

    public static int getPlaybackBuffer(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, getMaxBuffer returning " + original + "!");
            return original;
        }
        int retrievedValue = playbackMS.intValue();
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getPlaybackBuffer switched to: " + retrievedValue);
        }
        return retrievedValue;
    }

    public static int getReBuffer(int original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            Log.e("XGlobals", "Context is null, getMaxBuffer returning " + original + "!");
            return original;
        }
        int retrievedValue = reBuffer.intValue();
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getReBuffer switched to: " + retrievedValue);
        }
        return retrievedValue;
    }

    public static void InitializeFensterController(Context context, ViewGroup viewGroup, ViewConfiguration viewConfiguration) {
        fensterGestureController = new FensterGestureController();
        fensterGestureController.setFensterEventsListener(new XFenster(context, viewGroup), context, viewConfiguration);
        if (debug.booleanValue()) {
            Log.d("XGlobals", "XFenster initialized");
        }
    }

    public static boolean FensterTouchEvent(MotionEvent motionEvent) {
        if (fensterGestureController == null) {
            if (debug.booleanValue()) {
                Log.d("XGlobals", "fensterGestureController is null");
            }
            return false;
        } else if (motionEvent == null) {
            if (debug.booleanValue()) {
                Log.d("XGlobals", "motionEvent is null");
            }
            return false;
        } else if (!XSwipeHelper.IsControlsShown()) {
            return fensterGestureController.onTouchEvent(motionEvent);
        } else {
            if (debug.booleanValue()) {
                Log.d("XGlobals", "skipping onTouchEvent dispatching because controls are shown.");
            }
            return false;
        }
    }

    public static void PlayerTypeChanged(env playerType) {
        if (debug.booleanValue()) {
            Log.d("XDebug", playerType.toString());
        }
        if (PlayerType != playerType) {
            String playerTypeString = playerType.toString();
            if (playerTypeString.equals("WATCH_WHILE_FULLSCREEN")) {
                EnableXFenster();
            } else {
                DisableXFenster();
            }
            if (playerTypeString.equals("WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED") || playerTypeString.equals("WATCH_WHILE_MINIMIZED") || playerTypeString.equals("WATCH_WHILE_PICTURE_IN_PICTURE")) {
                NewSegmentHelperLayout.hide();
            }
            fi.vanced.libraries.youtube.player.PlayerType.playerTypeChanged(playerTypeString);
        }
        PlayerType = playerType;
    }

    public static void EnableXFenster() {
        if (EnableXFensterBrightness.booleanValue() || EnableXFensterVolume.booleanValue()) {
            FensterGestureController fensterGestureController2 = fensterGestureController;
            fensterGestureController2.TouchesEnabled = true;
            ((XFenster) fensterGestureController2.listener).enable(EnableXFensterBrightness.booleanValue(), EnableXFensterVolume.booleanValue());
        }
    }

    public static void DisableXFenster() {
        FensterGestureController fensterGestureController2 = fensterGestureController;
        fensterGestureController2.TouchesEnabled = false;
        ((XFenster) fensterGestureController2.listener).disable();
    }

    public static boolean isFensterBrightnessEnabled() {
        return EnableXFensterBrightness.booleanValue();
    }

    public static void CheckForMicroG(Activity activity) {
        AlertDialog.Builder builder;
        if (!appInstalledOrNot("com.mgoogle.android.gms")) {
            if (debug.booleanValue()) {
                Log.d("XDebug", "Custom MicroG installation undetected");
            }
            if (Build.VERSION.SDK_INT >= 21) {
                builder = new AlertDialog.Builder(activity, 16974374);
            } else {
                builder = new AlertDialog.Builder(activity);
            }
            builder.setTitle("Someone is not reading...").setMessage("You didn't install the MicroG as instructed, you can't login without it.\n\nInstall it and try again.").setPositiveButton("Close", new DialogInterface.OnClickListener() { // from class: fi.razerman.youtube.XGlobals.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                }
            }).show();
        } else if (debug.booleanValue()) {
            Log.i("XDebug", "Custom MicroG installation detected");
        }
    }

    public static boolean isFensterEnabled() {
        if (PlayerType != null && PlayerType.toString().equals("WATCH_WHILE_FULLSCREEN") && !XSwipeHelper.IsControlsShown()) {
            return EnableXFensterBrightness.booleanValue() || EnableXFensterVolume.booleanValue();
        }
        return false;
    }

    public static boolean isWatchWhileFullScreen() {
        if (PlayerType == null) {
            return false;
        }
        return PlayerType.toString().equals("WATCH_WHILE_FULLSCREEN");
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

    private static String getVersionName() {
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private static int appGetFirstTimeRun() {
        SharedPreferences appPreferences = getContext().getSharedPreferences("youtube_vanced", 0);
        String appCurrentBuildVersion = getVersionName();
        String appLastBuildVersion = appPreferences.getString("app_first_time", null);
        if (appLastBuildVersion == null || !appLastBuildVersion.equalsIgnoreCase(appCurrentBuildVersion)) {
            return appLastBuildVersion == null ? 0 : 2;
        }
        return 1;
    }

    public static void ChangeLogAndOfficialChecker(Activity activity) {
        AlertDialog.Builder builder;
        if (appGetFirstTimeRun() != 1) {
            final String versionName = getVersionName();
            String[] results = XJson.getVersion(versionName);
            String title = "Vanced Team";
            String message = "\n - xfileFIN\n - Laura Almeida \n - ZaneZam\n - KevinX8";
            String buttonPositive = "Close";
            String buttonNegative = "Remind later";
            if (results != null && results.length >= 3 && results[0] != null && !results[0].isEmpty() && results[1] != null && !results[1].isEmpty() && results[2] != null && !results[2].isEmpty()) {
                title = results[0];
                message = results[1];
                buttonPositive = results[2];
                buttonNegative = (results.length < 4 || results[3] == null || results[3].isEmpty()) ? null : results[3];
            }
            if (Build.VERSION.SDK_INT >= 21) {
                builder = new AlertDialog.Builder(activity, 16974374);
            } else {
                builder = new AlertDialog.Builder(activity);
            }
            builder.setTitle(title).setMessage(message).setPositiveButton(buttonPositive, new DialogInterface.OnClickListener() { // from class: fi.razerman.youtube.XGlobals.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences appPreferences = XGlobals.getContext().getSharedPreferences("youtube_vanced", 0);
                    appPreferences.edit().putString("app_first_time", versionName).apply();
                }
            }).setNegativeButton(buttonNegative, new DialogInterface.OnClickListener() { // from class: fi.razerman.youtube.XGlobals.2
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                }
            }).show();
        }
    }

    private static void UnofficialChecker(Activity activity) {
        AlertDialog.Builder builder;
        if (ExecuteShellCommand("grep -r m0yP /magisk/iYTBPforMagisk")) {
            if (Build.VERSION.SDK_INT >= 21) {
                builder = new AlertDialog.Builder(activity, 16974374);
            } else {
                builder = new AlertDialog.Builder(activity);
            }
            builder.setTitle("Unofficial Version").setMessage("This is an unofficial Magisk module.\nNo support is provided for this and it's adviced to download the official one from the following url.\nUrl: goo.gl/xW9u4U").setPositiveButton("Close", new DialogInterface.OnClickListener() { // from class: fi.razerman.youtube.XGlobals.4
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialog, int id) {
                }
            }).show();
        }
    }

    public static String getPackageName() {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (XFILEDEBUG.booleanValue() && context == null) {
            context = XSettingActivity.getAppContext();
        }
        if (context == null) {
            Log.e("XGlobals", "Context is null, returning com.google.android.youtube!");
            return "com.google.android.youtube";
        }
        String PACKAGE_NAME = context.getPackageName();
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getPackageName: " + PACKAGE_NAME);
        }
        return PACKAGE_NAME;
    }

    public static String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            Log.e("XGlobals", "Resource not found.", exception);
            return "";
        }
    }

    public static int getOverrideWidth(int original) {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        int compatibility = original;
        if (sharedPreferences.getBoolean("override_resolution_xfile_enabled", false)) {
            compatibility = 2160;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getOverrideWidth: " + compatibility);
        }
        return compatibility;
    }

    public static int getOverrideHeight(int original) {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context == null) {
            Log.e("XGlobals", "Context is null, returning " + original + "!");
            return original;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
        int compatibility = original;
        if (sharedPreferences.getBoolean("override_resolution_xfile_enabled", false)) {
            compatibility = 3840;
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "getOverrideHeight: " + compatibility);
        }
        return compatibility;
    }

    public static Context getContext() {
        ReadSettings();
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        if (context != null) {
            if (debug.booleanValue()) {
                Log.d("XGlobals", "getContext");
            }
            return context;
        } else if (XFILEDEBUG.booleanValue()) {
            return XSettingActivity.getAppContext();
        } else {
            Log.e("XGlobals", "Context is null, returning null!");
            return null;
        }
    }

    public static void setOldLayout(SharedPreferences sharedPreferences, String config, long timeStamp) {
        ReadSettings();
        if (!sharedPreferences.getBoolean("old_layout_xfile_enabled", false)) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group", config).putLong("com.google.android.libraries.youtube.innertube.cold_stored_timestamp", timeStamp).apply();
            if (debug.booleanValue()) {
                Log.d("XGlobals", "setOldLayout: true");
                return;
            }
            return;
        }
        if (sharedPreferences.contains("com.google.android.libraries.youtube.innertube.cold_config_group")) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group_backup", sharedPreferences.getString("com.google.android.libraries.youtube.innertube.cold_config_group", null)).remove("com.google.android.libraries.youtube.innertube.cold_config_group").apply();
        }
        if (debug.booleanValue()) {
            Log.d("XGlobals", "setOldLayout: false");
        }
    }

    public static void NewVideoStarted() {
        ReadSettings();
        newVideo = true;
        newVideoSpeed = true;
        if (debug.booleanValue()) {
            Log.d("XGlobals", "New video started!");
        }
    }

    public static boolean ExecuteShellCommand(String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = in.readLine();
            in.close();
            if (debug.booleanValue()) {
                Log.d("XDebug", "Command Output: " + line);
            }
            if (line.contains("m0yP")) {
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Exception e) {
                    }
                }
                return true;
            }
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e2) {
                }
            }
            return false;
        } catch (Exception e3) {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e4) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e5) {
                }
            }
            throw th;
        }
    }

    public static Object PrintSettings(Object[] settings) {
        Exception e;
        Exception e2;
        Class<?> stringType;
        ReadSettings();
        Class<?> stringType2 = String.class;
        if (debug.booleanValue()) {
            Log.d("Protobuf", "new settings array");
        }
        Object[] newArray = new Object[settings.length + 1];
        boolean found = false;
        int index = 0;
        try {
            if (debug.booleanValue()) {
                try {
                    Log.d("Protobuf", "Printing array");
                } catch (Exception e3) {
                    e = e3;
                    Log.e("Protobuf", "Error: " + e.getMessage());
                    return settings;
                }
            }
            try {
                try {
                    int i = 0;
                    String className = null;
                    for (Object settingObject : settings) {
                        try {
                            Field[] fields = settingObject.getClass().getFields();
                            int length = fields.length;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                }
                                Field field = fields[i2];
                                if (field.getType().isAssignableFrom(stringType2) && field.getName().equals("a")) {
                                    String value = field.get(settingObject).toString();
                                    if (value.equals("e")) {
                                        className = settingObject.getClass().getName();
                                        found = true;
                                        break;
                                    }
                                }
                                i2++;
                            }
                            index++;
                            if (found) {
                                break;
                            }
                        } catch (Exception e4) {
                            e2 = e4;
                            Log.e("Protobuf", "Error: " + e2.getMessage());
                            return settings;
                        }
                    }
                    if (found) {
                        if (debug.booleanValue()) {
                            Log.d("Protobuf", "Modifying array");
                        }
                        System.arraycopy(settings, 0, newArray, 0, index - 1);
                        Class<?> clazz = Class.forName(className);
                        Object object = clazz.newInstance();
                        newArray[index - 1] = object;
                        System.arraycopy(settings, index - 1, newArray, index, (settings.length - index) + 1);
                    } else {
                        newArray = settings;
                    }
                    int length2 = newArray.length;
                    boolean hitE = false;
                    int loop = 0;
                    int loop2 = 0;
                    while (loop2 < length2) {
                        Object settingObject2 = newArray[loop2];
                        Field[] fields2 = settingObject2.getClass().getFields();
                        int length3 = fields2.length;
                        boolean hitE2 = hitE;
                        while (i < length3) {
                            Field field2 = fields2[i];
                            if (!field2.getType().isAssignableFrom(stringType2)) {
                                stringType = stringType2;
                                length2 = length2;
                            } else if (field2.getName().equals("a")) {
                                if (loop != index - 1 || !found) {
                                    stringType = stringType2;
                                } else {
                                    if (debug.booleanValue()) {
                                        StringBuilder sb = new StringBuilder();
                                        stringType = stringType2;
                                        sb.append("String a field: ");
                                        sb.append(field2.get(settingObject2).toString());
                                        sb.append(" set: country-type");
                                        Log.d("Protobuf", sb.toString());
                                    } else {
                                        stringType = stringType2;
                                    }
                                    field2.set(settingObject2, "country-type");
                                }
                                String value2 = field2.get(settingObject2).toString();
                                if (value2.equals("e")) {
                                    hitE2 = true;
                                }
                                if (debug.booleanValue()) {
                                    StringBuilder sb2 = new StringBuilder();
                                    length2 = length2;
                                    sb2.append("String a field: ");
                                    sb2.append(value2);
                                    Log.d("Protobuf", sb2.toString());
                                } else {
                                    length2 = length2;
                                }
                            } else {
                                stringType = stringType2;
                                length2 = length2;
                                if (field2.getName().equals("b")) {
                                    if (loop == index - 1 && found) {
                                        if (debug.booleanValue()) {
                                            Log.d("Protobuf", "String b field: " + field2.get(settingObject2).toString() + " set: B");
                                        }
                                        field2.set(settingObject2, "B");
                                    }
                                    String value3 = field2.get(settingObject2).toString();
                                    if (hitE2) {
                                        field2.set(settingObject2, "11202606,9415293,9422596,9429003,9431755,9435797,9442923,9444108,9444635,9449243,9453077,9456940,9463829,9464088,9467503,9476327,9477614,9478523,9480475,9480495,9482942,9483422,9483531,9484706,9485998,9487653,9487664,9488038,9488230,9489113");
                                        hitE2 = false;
                                    }
                                    if (debug.booleanValue()) {
                                        Log.d("Protobuf", "String b field: " + value3);
                                    }
                                } else {
                                    String value4 = field2.get(settingObject2).toString();
                                    if (debug.booleanValue()) {
                                        Log.d("Protobuf", "String field: " + field2.getName() + " = " + value4);
                                    }
                                }
                            }
                            i++;
                            stringType2 = stringType;
                        }
                        loop++;
                        loop2++;
                        hitE = hitE2;
                        i = 0;
                    }
                    return newArray;
                } catch (Exception e5) {
                    e = e5;
                    Log.e("Protobuf", "Error: " + e.getMessage());
                    return settings;
                }
            } catch (Exception e6) {
                e2 = e6;
            }
        } catch (Exception e7) {
            e = e7;
        }
        return null;
    }

    public static Object PrintSettings(Object[] settings, int type) {
        Exception e;
        int i = 0;
        boolean found;
        Class<?> stringType;
        ReadSettings();
        boolean modifyArray = type == 2;
        Class<?> stringType2 = String.class;
        if (debug.booleanValue()) {
            Log.d("Protobuf", "new settings array");
        }
        Object[] newArray = new Object[settings.length + 1];
        if (!modifyArray) {
            newArray = settings;
        }
        boolean found2 = false;
        int index = 0;
        if (modifyArray) {
            try {
                if (debug.booleanValue()) {
                    Log.d("Protobuf", "Modifying array");
                }
                try {
                    int length = settings.length;
                    int index2 = 0;
                    boolean found3 = false;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length) {
                            found2 = found3;
                            index = index2;
                            break;
                        }
                        try {
                            Object settingObject = settings[i2];
                            Field[] fields = settingObject.getClass().getFields();
                            int length2 = fields.length;
                            int i3 = 0;
                            while (true) {
                                if (i3 >= length2) {
                                    break;
                                }
                                Field field = fields[i3];
                                if (field.getType().isAssignableFrom(stringType2) && field.getName().equals("a")) {
                                    String value = field.get(settingObject).toString();
                                    if (value.equals("e")) {
                                        found3 = true;
                                        break;
                                    }
                                }
                                i3++;
                            }
                            index2++;
                            if (found3) {
                                found2 = found3;
                                index = index2;
                                break;
                            }
                            i2++;
                        } catch (Exception e2) {
                            e = e2;
                            Log.e("Protobuf", "Error: " + e.getMessage());
                            return settings;
                        }
                    }
                    i = 0;
                    System.arraycopy(settings, 0, newArray, 0, index);
                    System.arraycopy(settings, index - 1, newArray, index, (settings.length - index) + 1);
                } catch (Exception e3) {
                    e = e3;
                }
            } catch (Exception e4) {
                e = e4;
                Log.e("Protobuf", "Error: " + e.getMessage());
                return settings;
            }
        } else {
            i = 0;
            newArray = settings;
        }
        try {
            int length3 = newArray.length;
            int loop = 0;
            int loop2 = i;
            while (loop2 < length3) {
                Object settingObject2 = newArray[loop2];
                Field[] fields2 = settingObject2.getClass().getFields();
                int length4 = fields2.length;
                while (i < length4) {
                    Field field2 = fields2[i];
                    if (!field2.getType().isAssignableFrom(stringType2)) {
                        stringType = stringType2;
                        found = found2;
                    } else if (field2.getName().equals("a")) {
                        if (loop == index - 1 && modifyArray) {
                            field2.set(settingObject2, "country-type");
                        }
                        String value2 = field2.get(settingObject2).toString();
                        if (debug.booleanValue()) {
                            stringType = stringType2;
                            try {
                                StringBuilder sb = new StringBuilder();
                                found = found2;
                                sb.append("String a field: ");
                                sb.append(value2);
                                Log.d("Protobuf", sb.toString());
                            } catch (Exception e5) {
                                e = e5;
                                Log.e("Protobuf", "Error: " + e.getMessage());
                                return settings;
                            }
                        } else {
                            stringType = stringType2;
                            found = found2;
                        }
                    } else {
                        stringType = stringType2;
                        found = found2;
                        if (field2.getName().equals("b")) {
                            if (loop == index - 1 && modifyArray) {
                                field2.set(settingObject2, "B");
                            }
                            String value3 = field2.get(settingObject2).toString();
                            if (debug.booleanValue()) {
                                Log.d("Protobuf", "String b field: " + value3);
                            }
                        } else {
                            String value4 = field2.get(settingObject2).toString();
                            if (debug.booleanValue()) {
                                Log.d("Protobuf", "String field: " + field2.getName() + " = " + value4);
                            }
                        }
                    }
                    i++;
                    stringType2 = stringType;
                    found2 = found;
                }
                loop++;
                loop2++;
                i = 0;
            }
            return newArray;
        } catch (Exception e6) {
            e = e6;
        }
        return null;
    }

    public static void PrintVideoQualities(Object quality, int mode) {
        Class<?> intType;
        Class<?> stringType;
        Field fieldArray;
        ReadSettings();
        if (debug.booleanValue()) {
            Log.d("VideoQualities", "Quality parameter: " + mode);
        }
        if (mode == 0) {
            Class<?> intType2 = Integer.TYPE;
            Class<?> stringType2 = String.class;
            Class<?> boolType = Boolean.TYPE;
            try {
                Class<?> clazz = quality.getClass();
                Field fieldArray2 = clazz.getField("e");
                Object[] fieldValue = (Object[]) fieldArray2.get(quality);
                ArrayList<Integer> iStreamQualities = new ArrayList<>();
                ArrayList<String> sStreamQualities = new ArrayList<>();
                ArrayList<Boolean> bStreamQualities = new ArrayList<>();
                int length = fieldValue.length;
                int i = 0;
                while (i < length) {
                    Object streamQuality = fieldValue[i];
                    Field[] fields = streamQuality.getClass().getFields();
                    int length2 = fields.length;
                    int i2 = 0;
                    while (i2 < length2) {
                        Field field = fields[i2];
                        if (field.getType().isAssignableFrom(intType2)) {
                            try {
                                int value = field.getInt(streamQuality);
                                intType = intType2;
                                try {
                                    int length3 = field.getName().length();
                                    fieldArray = fieldArray2;
                                    if (length3 <= 2) {
                                        iStreamQualities.add(Integer.valueOf(value));
                                    }
                                    if (debug.booleanValue()) {
                                        Log.d("VideoQualities", "Integer field: " + field.getName() + " = " + value);
                                    }
                                    stringType = stringType2;
                                } catch (Exception e) {
                                    return;
                                }
                            } catch (Exception e2) {
                                return;
                            }
                        } else {
                            intType = intType2;
                            fieldArray = fieldArray2;
                            try {
                                if (field.getType().isAssignableFrom(stringType2)) {
                                    String value2 = field.get(streamQuality).toString();
                                    sStreamQualities.add(value2);
                                    if (debug.booleanValue()) {
                                        StringBuilder sb = new StringBuilder();
                                        stringType = stringType2;
                                        try {
                                            sb.append("String field: ");
                                            sb.append(field.getName());
                                            sb.append(" = ");
                                            sb.append(value2);
                                            Log.d("VideoQualities", sb.toString());
                                        } catch (Exception e3) {
                                            return;
                                        }
                                    } else {
                                        stringType = stringType2;
                                    }
                                } else {
                                    stringType = stringType2;
                                    if (field.getType().isAssignableFrom(boolType)) {
                                        boolean value3 = field.getBoolean(streamQuality);
                                        bStreamQualities.add(Boolean.valueOf(value3));
                                        if (debug.booleanValue()) {
                                            Log.d("VideoQualities", "Boolean field: " + field.getName() + " = " + value3);
                                        }
                                    }
                                }
                            } catch (Exception e4) {
                                return;
                            }
                        }
                        i2++;
                        fieldArray2 = fieldArray;
                        stringType2 = stringType;
                        intType2 = intType;
                    }
                    i++;
                    clazz = clazz;
                }
            } catch (Exception e5) {
            }
        }
    }

    public static void PrintQualities(Object[] qualities, int quality) {
        ArrayList<Integer> iStreamQualities;
        Class<?> intType;
        Object[] objArr = qualities;
        ReadSettings();
        Class<?> intType2 = Integer.TYPE;
        Class<?> boolType = Boolean.TYPE;
        if (debug.booleanValue()) {
            Log.d("QUALITY", "Quality parameter: " + quality);
        }
        try {
            ArrayList<Integer> iStreamQualities2 = new ArrayList<>();
            ArrayList<String> sStreamQualities = new ArrayList<>();
            ArrayList<Boolean> bStreamQualities = new ArrayList<>();
            int length = objArr.length;
            int i = 0;
            while (i < length) {
                Object streamQuality = objArr[i];
                Field[] fields = streamQuality.getClass().getFields();
                int length2 = fields.length;
                int i2 = 0;
                while (i2 < length2) {
                    Field field = fields[i2];
                    if (field.getType().isAssignableFrom(intType2)) {
                        int value = field.getInt(streamQuality);
                        intType = intType2;
                        if (field.getName().length() <= 2) {
                            try {
                                iStreamQualities2.add(Integer.valueOf(value));
                            } catch (Exception e) {
                                return;
                            }
                        }
                        if (debug.booleanValue()) {
                            StringBuilder sb = new StringBuilder();
                            iStreamQualities = iStreamQualities2;
                            sb.append("Integer field: ");
                            sb.append(field.getName());
                            sb.append(" = ");
                            sb.append(value);
                            Log.d("QUALITY", sb.toString());
                        } else {
                            iStreamQualities = iStreamQualities2;
                        }
                    } else {
                        iStreamQualities = iStreamQualities2;
                        intType = intType2;
                        if (field.getType().isAssignableFrom(String.class)) {
                            String value2 = field.get(streamQuality).toString();
                            sStreamQualities.add(value2);
                            if (debug.booleanValue()) {
                                Log.d("QUALITY", "String field: " + field.getName() + " = " + value2);
                            }
                        } else if (field.getType().isAssignableFrom(boolType)) {
                            boolean value3 = field.getBoolean(streamQuality);
                            bStreamQualities.add(Boolean.valueOf(value3));
                            if (debug.booleanValue()) {
                                Log.d("QUALITY", "Boolean field: " + field.getName() + " = " + value3);
                            }
                        }
                    }
                    i2++;
                    intType2 = intType;
                    iStreamQualities2 = iStreamQualities;
                }
                i++;
                objArr = qualities;
            }
        } catch (Exception e2) {
        }
    }

    public static ColorStateList getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        int colorRes = typedValue.resourceId;
        int color = -1;
        try {
            color = context.getResources().getColor(colorRes);
        } catch (Resources.NotFoundException e) {
            Log.w("XGlobals", "Not found color resource by id: " + colorRes);
        }
        int[][] states = {new int[]{16842910}, new int[]{-16842910}, new int[]{-16842912}, new int[]{16842919}};
        int[] colors = {color, color, color, color};
        ColorStateList myList = new ColorStateList(states, colors);
        return myList;
    }
}
