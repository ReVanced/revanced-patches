package app.revanced.integrations.tiktok;

import app.revanced.integrations.shared.settings.StringSetting;

public class Utils {

    // Edit: This could be handled using a custom Setting<Long[]> class
    // that saves its value to preferences and JSON using the formatted String created here.
    public static long[] parseMinMax(StringSetting setting) {
        final String[] minMax = setting.get().split("-");
        if (minMax.length == 2) {
            try {
                final long min = Long.parseLong(minMax[0]);
                final long max = Long.parseLong(minMax[1]);

                if (min <= max && min >= 0) return new long[]{min, max};

            } catch (NumberFormatException ignored) {
            }
        }

        setting.save("0-" + Long.MAX_VALUE);
        return new long[]{0L, Long.MAX_VALUE};
    }
}