package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

final class ButtonsPatch extends Filter {
    private final BlockRule actionButtonsRule;
    private final BlockRule dislikeRule;
    private final BlockRule actionBarRule;

    private final BlockRule[] rules;

    public ButtonsPatch() {
        BlockRule like = new BlockRule(SettingsEnum.HIDE_LIKE_BUTTON, "|like_button");
        dislikeRule = new BlockRule(SettingsEnum.HIDE_DISLIKE_BUTTON, "dislike_button");
        BlockRule download = new BlockRule(SettingsEnum.HIDE_DOWNLOAD_BUTTON, "download_button");
        actionButtonsRule = new BlockRule(SettingsEnum.HIDE_ACTION_BUTTON, "ContainerType|video_action_button");
        BlockRule playlist = new BlockRule(SettingsEnum.HIDE_PLAYLIST_BUTTON, "save_to_playlist_button");
        rules = new BlockRule[]{like, dislikeRule, download, actionButtonsRule, playlist};

        actionBarRule = new BlockRule(null, "video_action_bar");

        this.pathRegister.registerAll(
                like,
                dislikeRule,
                download,
                playlist
        );
    }

    private boolean hideActionBar() {
        for (BlockRule rule : rules) if (!rule.isEnabled()) return false;
        return true;
    }

    @Override
    public boolean filter(final String path, final String identifier) {
        if (hideActionBar() && actionBarRule.check(identifier).isBlocked()) return true;

        var currentIsActionButton = actionButtonsRule.check(path).isBlocked();

        if (dislikeRule.check(path).isBlocked()) ActionButton.doNotBlockCounter = 4;

        if (currentIsActionButton && ActionButton.doNotBlockCounter-- > 0) {
            if (SettingsEnum.HIDE_SHARE_BUTTON.getBoolean()) {
                LogHelper.debug(ButtonsPatch.class, "Hiding share button");
                return true;
            } else return false;
        }

        if ((currentIsActionButton && ActionButton.doNotBlockCounter <= 0 && actionButtonsRule.isEnabled()) || pathRegister.contains(path)) {
            LogHelper.debug(ButtonsPatch.class, "Blocked: " + path);
            return true;
        } else return false;
    }

    static class ActionButton {
        public static int doNotBlockCounter = 4;
    }
}
