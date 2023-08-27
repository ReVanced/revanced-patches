package app.revanced.integrations.patches;

import android.widget.ImageView;

import app.revanced.integrations.settings.SettingsEnum;

public class HidePlayerOverlayPatch {
    public static void hidePlayerOverlay(ImageView view) {
        if (!SettingsEnum.HIDE_PLAYER_OVERLAY.getBoolean()) return;
        view.setImageResource(android.R.color.transparent);
    }
}
