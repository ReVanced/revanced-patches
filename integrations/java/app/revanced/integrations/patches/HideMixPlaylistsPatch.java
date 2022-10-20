package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideMixPlaylistsPatch {

    public static void hideMixPlaylists(View view) {
        if (!SettingsEnum.HIDE_MIX_PLAYLISTS.getBoolean()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}
