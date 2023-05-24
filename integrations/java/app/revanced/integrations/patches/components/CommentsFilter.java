package app.revanced.integrations.patches.components;

import app.revanced.integrations.settings.SettingsEnum;

final class CommentsFilter extends Filter {

    public CommentsFilter() {
        var comments = new StringFilterGroup(
                SettingsEnum.HIDE_COMMENTS_SECTION,
                "video_metadata_carousel",
                "_comments"
        );

        var previewComment = new StringFilterGroup(
                SettingsEnum.HIDE_PREVIEW_COMMENT,
                "|carousel_item",
                "comments_entry_point_teaser",
                "comments_entry_point_simplebox"
        );

        this.pathFilterGroups.addAll(
                comments,
                previewComment
        );
    }
}
