package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public class HideAlbumCardsPatch {
    public static void hideAlbumCard(View view) {
        if (!SettingsEnum.HIDE_ALBUM_CARDS.getBoolean()) return;
        ReVancedUtils.hideViewByLayoutParams(view);
    }
}