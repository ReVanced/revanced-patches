package pl.jakubweg;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class Helper {

    public static String getStringByName(Context context, String name) {
        try {
            Resources res = context.getResources();
            return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
        } catch (Throwable exception) {
            Log.e("XGlobals", "Resource not found.", exception);
            return "";
        }
    }

}
