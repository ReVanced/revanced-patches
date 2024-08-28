package app.revanced.extension.youtube.patches;

import android.view.View;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public class HideAlbumCardsPatch {
    public static void hideAlbumCard(View view) {
        if (!Settings.HIDE_ALBUM_CARDS.get()) return;
        Utils.hideViewByLayoutParams(view);
    }
}