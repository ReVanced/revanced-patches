package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideBreakingNewsPatch {
    //Used by app.revanced.patches.youtube.layout.homepage.breakingnews.patch.BreakingNewsPatch
    public static void hideBreakingNews(View view) {
        if (!SettingsEnum.HIDE_BREAKING_NEWS.getBoolean()) return;
        AdRemoverAPI.HideViewWithLayout1dp(view);
    }
}
