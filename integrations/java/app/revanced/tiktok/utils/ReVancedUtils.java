package app.revanced.tiktok.utils;

import android.content.Context;

public class ReVancedUtils {

    //Used by TiktokIntegrations patch
    public static Context context;

    //Used by TiktokIntegrations patch
    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException(ReVancedUtils.class, "Context is null!");
        return null;
    }
}