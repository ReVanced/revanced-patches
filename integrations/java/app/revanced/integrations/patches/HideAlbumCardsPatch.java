package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideAlbumCardsPatch {
    //Used by app.revanced.patches.youtube.layout.hidealbumcards.patch.HideAlbumCardsPatch
    public static void hideAlbumCards(View view) {
        if (!SettingsEnum.HIDE_ALBUM_CARDS.getBoolean()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}