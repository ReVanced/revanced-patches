package fi.razerman.youtube.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/* loaded from: classes6.dex */
public class XSharedPrefs {
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        try {
            if (context == null) {
                Log.e("XSharedPrefs", "context is null");
                return false;
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("youtube", 0);
            return sharedPreferences.getBoolean(key, defValue);
        } catch (Exception ex) {
            Log.e("XSharedPrefs", "Error getting boolean", ex);
            return defValue;
        }
    }
}
