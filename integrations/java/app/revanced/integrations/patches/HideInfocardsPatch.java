package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfocardsPatch {
    public static void hideInfocardsIncognito(View view) {
        if (!SettingsEnum.HIDE_INFO_CARDS.getBoolean()) return;
        view.setVisibility(View.GONE);
    }

    public static boolean hideInfocardsMethodCall() {
        return SettingsEnum.HIDE_INFO_CARDS.getBoolean();
    }
}
