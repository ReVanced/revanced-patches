package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideInfoCardsFilter extends Filter {

    public HideInfoCardsFilter() {
        addIdentifierCallbacks(
                new StringFilterGroup(
                        Settings.HIDE_INFO_CARDS,
                        "info_card_teaser_overlay.e"
                )
        );
    }
}
