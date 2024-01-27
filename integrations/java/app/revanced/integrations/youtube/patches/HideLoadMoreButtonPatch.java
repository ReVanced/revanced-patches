package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Utils;

@SuppressWarnings("unused")
public class HideLoadMoreButtonPatch {
    public static void hideLoadMoreButton(View view){
        if(!Settings.HIDE_LOAD_MORE_BUTTON.get()) return;
        Utils.hideViewByLayoutParams(view);
    }
}
