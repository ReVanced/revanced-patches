package app.revanced.integrations.youtube.patches.components;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideInfoCardsFilterPatch extends Filter {

    public HideInfoCardsFilterPatch() {
        addIdentifierCallbacks(
                new StringFilterGroup(
                        Settings.HIDE_INFO_CARDS,
                        "info_card_teaser_overlay.eml"
                )
        );
    }
}
