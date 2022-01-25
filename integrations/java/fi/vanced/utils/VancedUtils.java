package fi.vanced.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.security.SecureRandom;

public class VancedUtils {

    private VancedUtils() {}

    public static SharedPreferences getPreferences(Context context, String preferencesName) {
        if (context == null) return null;
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }

    public static int getIdentifier(String name, String defType) {
        Context context = YouTubeTikTokRoot_Application.getAppContext();
        return context.getResources().getIdentifier(name, defType, context.getPackageName());
    }

    // https://stackoverflow.com/a/157202
    static final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static int countMatches(CharSequence seq, char c) {
        int count = 0;
        for (int i = 0; i < seq.length(); i++) {
            if (seq.charAt(i) == c)
                count++;
        }
        return count;
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return (version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ("17.03.35");
    }

    public static void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}