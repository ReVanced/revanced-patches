package app.revanced.tiktok.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import app.revanced.tiktok.settings.SettingsEnum;

public class ReVancedUtils {

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static Context getAppContext() {
        if (context != null) {
            return context;
        }
        LogHelper.printException(ReVancedUtils.class, "Context is null!");
        return null;
    }

    public static long[] parseMinMax(SettingsEnum setting) {
        if (setting.returnType == SettingsEnum.ReturnType.STRING) {
            final String[] minMax = setting.getString().split("-");

            if (minMax.length == 2)
                try {
                    final long min = Long.parseLong(minMax[0]);
                    final long max = Long.parseLong(minMax[1]);

                    if (min <= max && min >= 0) return new long[]{min, max};

                } catch (NumberFormatException ignored) {
                }
        }

        setting.saveValue("0-" + Long.MAX_VALUE);
        return new long[]{0L, Long.MAX_VALUE};
    }
}