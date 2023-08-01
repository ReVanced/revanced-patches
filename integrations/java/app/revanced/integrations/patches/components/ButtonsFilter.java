package app.revanced.integrations.patches.components;

import androidx.annotation.Nullable;

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
                ),
                actionBarRule
        );
    }

    private boolean isEveryFilterGroupEnabled() {
        for (StringFilterGroup rule : pathFilterGroups)
            if (!rule.isEnabled()) return false;

        return true;
    }

    @Override
    public boolean isFiltered(String path, @Nullable String identifier, byte[] protobufBufferArray,
                              FilterGroupList matchedList, FilterGroup matchedGroup, int matchedIndex) {
        if (matchedGroup == actionBarRule) {
            return isEveryFilterGroupEnabled();
        }

        return super.isFiltered(path, identifier, protobufBufferArray, matchedList, matchedGroup, matchedIndex);
    }
}
