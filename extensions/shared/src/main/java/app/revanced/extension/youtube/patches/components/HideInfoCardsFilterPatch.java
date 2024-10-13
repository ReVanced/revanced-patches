package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.youtube.settings.Settings;

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
