package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideInfoCardSuggestionsPatch {

    public static int hideInfoCardSuggestions() {
        return SettingsEnum.INFO_CARDS_SHOWN_BOOLEAN.getBoolean() ? 0 : 8;
    }
}
