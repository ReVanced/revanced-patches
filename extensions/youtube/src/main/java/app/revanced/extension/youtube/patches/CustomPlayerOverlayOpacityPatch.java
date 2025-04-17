package app.revanced.extension.youtube.patches;

import static app.revanced.extension.shared.StringRef.str;

import android.widget.ImageView;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class CustomPlayerOverlayOpacityPatch {

    private static final int PLAYER_OVERLAY_OPACITY_LEVEL;

    static {
        int opacity = Settings.PLAYER_OVERLAY_OPACITY.get();

        if (opacity < 0 || opacity > 100) {
            Utils.showToastLong(str("revanced_player_overlay_opacity_invalid_toast"));
            opacity = Settings.PLAYER_OVERLAY_OPACITY.resetToDefault();
        }

        PLAYER_OVERLAY_OPACITY_LEVEL = (opacity * 255) / 100;
    }

    /**
     * Injection point.
     */
    public static void changeOpacity(ImageView imageView) {
        imageView.setImageAlpha(PLAYER_OVERLAY_OPACITY_LEVEL);
    }
}
