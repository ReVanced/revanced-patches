package app.revanced.integrations.youtube.patches;

import android.widget.ImageView;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;

@SuppressWarnings("unused")
public class CustomPlayerOverlayOpacityPatch {

    public static void changeOpacity(ImageView imageView) {
        int opacity = Settings.PLAYER_OVERLAY_OPACITY.get();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong("Player overlay opacity must be between 0-100");
            Settings.PLAYER_OVERLAY_OPACITY.resetToDefault();
            opacity = Settings.PLAYER_OVERLAY_OPACITY.defaultValue;
        }

        imageView.setImageAlpha((opacity * 255) / 100);
    }
}
