package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

final class ButtonsFilter extends Filter {
    private final StringFilterGroup actionBarRule;

    public ButtonsFilter() {
        actionBarRule = new StringFilterGroup(
                null,
                "video_action_bar"
        );

        pathFilterGroups.addAll(
                new StringFilterGroup(
                        SettingsEnum.HIDE_LIKE_DISLIKE_BUTTON,
                        "|like_button",
                        "dislike_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_DOWNLOAD_BUTTON,
                        "download_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_PLAYLIST_BUTTON,
                        "save_to_playlist_button"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_CLIP_BUTTON,
                        "|clip_button.eml|"
                ),
                new StringFilterGroup(
                        SettingsEnum.HIDE_ACTION_BUTTONS,
                        "ContainerType|video_action_button",
                        "|CellType|CollectionType|CellType|ContainerType|button.eml|"
                )
        );
    }

    private boolean isEveryFilterGroupEnabled() {
        for (StringFilterGroup rule : pathFilterGroups)
            if (!rule.isEnabled()) return false;

        return true;
    }

    @Override
    public boolean isFiltered(final String path, final String identifier, final byte[] _protobufBufferArray) {
        if (isEveryFilterGroupEnabled())
            if (actionBarRule.check(identifier).isFiltered()) return true;

        return super.isFiltered(path, identifier, _protobufBufferArray);
    }
}
