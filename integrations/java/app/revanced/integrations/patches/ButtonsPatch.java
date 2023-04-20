package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

final class ButtonsPatch extends Filter {
    private final BlockRule actionBarRule;

    public ButtonsPatch() {
        actionBarRule = new BlockRule(null, "video_action_bar");
        pathRegister.registerAll(
                new BlockRule(SettingsEnum.HIDE_LIKE_DISLIKE_BUTTON, "|like_button", "dislike_button"),
                new BlockRule(SettingsEnum.HIDE_DOWNLOAD_BUTTON, "download_button"),
                new BlockRule(SettingsEnum.HIDE_PLAYLIST_BUTTON, "save_to_playlist_button"),
                new BlockRule(SettingsEnum.HIDE_ACTION_BUTTONS, "ContainerType|video_action_button")
        );
    }

    private boolean canHideActionBar() {
        for (BlockRule rule : pathRegister) if (!rule.isEnabled()) return false;
        return true;
    }

    @Override
    public boolean filter(final String path, final String identifier) {
        // If everything is hidden, then also hide the video bar itself.
        if (canHideActionBar() && actionBarRule.check(identifier).isBlocked()) return true;

        return pathRegister.contains(path);
    }
}
