package app.revanced.integrations.patches;

import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class CustomPlayerOverlayOpacityPatch {
    private static final int DEFAULT_OPACITY = (int) SettingsEnum.PLAYER_OVERLAY_OPACITY.defaultValue;

    public static void changeOpacity(ImageView imageView) {
        int opacity = SettingsEnum.PLAYER_OVERLAY_OPACITY.getInt();

        if (opacity < 0 || opacity > 100) {
            ReVancedUtils.showToastLong("Player overlay opacity must be between 0-100");
            SettingsEnum.PLAYER_OVERLAY_OPACITY.saveValue(DEFAULT_OPACITY);
            opacity = DEFAULT_OPACITY;
        }

        imageView.setImageAlpha((opacity * 255) / 100);
    }
}
