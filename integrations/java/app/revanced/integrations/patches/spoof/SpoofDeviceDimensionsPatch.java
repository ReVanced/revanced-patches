package app.revanced.integrations.patches.spoof;

import app.revanced.integrations.settings.SettingsEnum;

public class SpoofDeviceDimensionsPatch {
    private static final boolean SPOOF = SettingsEnum.SPOOF_DEVICE_DIMENSIONS.getBoolean();
    public static int getMinHeightOrWidth(int minHeightOrWidth) {
        return SPOOF ? 64 : minHeightOrWidth;
    }

    public static int getMaxHeightOrWidth(int maxHeightOrWidth) {
        return SPOOF ? 4096 : maxHeightOrWidth;
    }
}
