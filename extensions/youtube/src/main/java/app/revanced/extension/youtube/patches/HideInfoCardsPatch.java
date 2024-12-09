package app.revanced.extension.youtube.patches;

import android.view.View;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideInfoCardsPatch {
    public static void hideInfoCardsIncognito(View view) {
        if (!Settings.HIDE_INFO_CARDS.get()) return;
        view.setVisibility(View.GONE);
    }

    public static boolean hideInfoCardsMethodCall() {
        return Settings.HIDE_INFO_CARDS.get();
    }
}
