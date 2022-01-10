package fi.vanced.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.SecureRandom;

public class VancedUtils {

    public static SharedPreferences getPreferences(Context context, String preferencesName) {
        if (context == null) return null;
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }

    public static String parseJson(HttpURLConnection connection) throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        inputStream.close();
        return jsonBuilder.toString();
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
}