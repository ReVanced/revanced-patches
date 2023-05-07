package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideLoadMoreButtonPatch {
    public static void hideLoadMoreButton(View view){
        if(!SettingsEnum.HIDE_LOAD_MORE_BUTTON.getBoolean()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}
