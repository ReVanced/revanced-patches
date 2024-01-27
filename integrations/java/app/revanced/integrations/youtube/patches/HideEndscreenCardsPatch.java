package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideEndscreenCardsPatch {
    //Used by app.revanced.patches.youtube.layout.hideendscreencards.bytecode.patch.HideEndscreenCardsPatch
    public static void hideEndscreen(View view) {
        if (!Settings.HIDE_ENDSCREEN_CARDS.get()) return;
        view.setVisibility(View.GONE);
    }
}