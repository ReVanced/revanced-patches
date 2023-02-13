package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

final class CommentsPatch extends Filter {

    public CommentsPatch() {
        var comments = new BlockRule(SettingsEnum.HIDE_COMMENTS_SECTION, "video_metadata_carousel", "_comments");
        var previewComment = new BlockRule(
                SettingsEnum.HIDE_PREVIEW_COMMENT,
                "|carousel_item",
                "comments_entry_point_teaser",
                "comments_entry_point_simplebox"
        );

        this.pathRegister.registerAll(
                comments,
                previewComment
        );
    }

    @Override
    boolean filter(String path, String _identifier) {
        if (!pathRegister.contains(path)) return false;

        LogHelper.printDebug(() -> "Blocked: " + path);

        return true;
    }
}
