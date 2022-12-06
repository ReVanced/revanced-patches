package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class BrandingWaterMarkPatch {

    // Used by: app.revanced.patches.youtube.layout.watermark.patch.HideWatermarkPatch
    public static boolean isBrandingWatermarkShown() {
        return SettingsEnum.HIDE_VIDEO_WATERMARK.getBoolean() == false;
    }
}
