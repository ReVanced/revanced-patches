package app.revanced.integrations;

import android.content.Context;
import android.util.Log;

public class Globals {
    public static Context context;

    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        Log.e("Globals", "Context is null!");
        return null;
    }
}