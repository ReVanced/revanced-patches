package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

@SuppressWarnings("unused")
public final class HideInfoCardsFilterPatch extends Filter {

    public HideInfoCardsFilterPatch() {
        addIdentifierCallbacks(
                new StringFilterGroup(
                        SettingsEnum.HIDE_INFO_CARDS,
                        "info_card_teaser_overlay.eml"
                )
        );
    }
}
