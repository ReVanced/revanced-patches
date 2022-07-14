package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardSuggestionsPatch {

    public static int hideInfoCardSuggestions() {
        return SettingsEnum.INFO_CARDS_SHOWN.getBoolean() ? View.VISIBLE : View.GONE;
    }
}
