package app.revanced.integrations.patches;

import android.view.View;
import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardsPatch {
    public static void hideInfoCardsIncognito(View view) {
        if (!SettingsEnum.HIDE_INFO_CARDS.getBoolean()) return;
        view.setVisibility(View.GONE);
    }

    public static boolean hideInfoCardsMethodCall() {
        return SettingsEnum.HIDE_INFO_CARDS.getBoolean();
    }
}
