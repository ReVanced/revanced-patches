package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

public final class HideInfoCardsFilterPatch extends Filter {

    public HideInfoCardsFilterPatch() {
        identifierFilterGroupList.addAll(
                new StringFilterGroup(
                        SettingsEnum.HIDE_INFO_CARDS,
                        "info_card_teaser_overlay.eml"
                )
        );
    }
}
