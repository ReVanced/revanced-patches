package fi.vanced.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {
    public static void saveString(Context context, String preferenceName, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }
    public static void saveBoolean(Context context, String preferenceName, String key, Boolean value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }
    public static void saveInt(Context context, String preferenceName, String key, Integer value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static String getString(Context context, String preferenceName, String key){
        return getString(context, preferenceName, key, null);
    }
    public static String getString(Context context, String preferenceName, String key, String _default){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        return (sharedPreferences.getString(key, _default));
    }

    public static Boolean getBoolean(Context context, String preferenceName, String key){
        return getBoolean(context, preferenceName, key, false);
    }
    public static Boolean getBoolean(Context context, String preferenceName, String key, Boolean _default){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        return (sharedPreferences.getBoolean(key, _default));
    }

    public static Integer getInt(Context context, String preferenceName, String key){
        return getInt(context, preferenceName, key, -1);
    }
    public static Integer getInt(Context context, String preferenceName, String key, Integer _default){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        return (sharedPreferences.getInt(key, _default));
    }
}
