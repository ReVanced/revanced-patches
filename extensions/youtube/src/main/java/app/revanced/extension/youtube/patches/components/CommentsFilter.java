package app.revanced.extension.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
final class CommentsFilter extends Filter {

    private static final String TIMESTAMP_OR_EMOJI_BUTTONS_ENDS_WITH_PATH
            = "|CellType|ContainerType|ContainerType|ContainerType|ContainerType|ContainerType|";

    private final StringFilterGroup commentComposer;
    private final ByteArrayFilterGroup emojiPickerBufferGroup;
    private final StringFilterGroup filterChipBar;
    private final ByteArrayFilterGroup aiCommentsSummary;
    
    public CommentsFilter() {
        var chatSummary = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_CHAT_SUMMARY,
                "live_chat_summary_banner.eml"
        );

        var commentsByMembers = new StringFilterGroup(
                Settings.HIDE_COMMENTS_BY_MEMBERS_HEADER,
                "sponsorships_comments_header.eml",
                "sponsorships_comments_footer.eml"
        );

        var comments = new StringFilterGroup(
                Settings.HIDE_COMMENTS_SECTION,
                "video_metadata_carousel",
                "_comments"
        );

        var createAShort = new StringFilterGroup(
                Settings.HIDE_COMMENTS_CREATE_A_SHORT_BUTTON,
                "composer_short_creation_button.eml"
        );

        var previewComment = new StringFilterGroup(
                Settings.HIDE_COMMENTS_PREVIEW_COMMENT,
                "|carousel_item",
                "comments_entry_point_teaser",
                "comments_entry_point_simplebox"
        );

        var thanksButton = new StringFilterGroup(
                Settings.HIDE_COMMENTS_THANKS_BUTTON,
                "super_thanks_button.eml"
        );

        commentComposer = new StringFilterGroup(
                Settings.HIDE_COMMENTS_TIMESTAMP_AND_EMOJI_BUTTONS,
                "comment_composer.eml"
        );

        emojiPickerBufferGroup = new ByteArrayFilterGroup(
                null,
                "id.comment.quick_emoji.button"
        );

        filterChipBar = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_SUMMARY,
                "filter_chip_bar.eml"
        );

        aiCommentsSummary = new ByteArrayFilterGroup(
                null,
                "yt_fill_spark_"
        );

        addPathCallbacks(
                chatSummary,
                commentsByMembers,
                comments,
                createAShort,
                previewComment,
                thanksButton,
                commentComposer,
                filterChipBar
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == commentComposer) {
            // To completely hide the emoji buttons (and leave no empty space), the timestamp button is
            // also hidden because the buffer is exactly the same and there's no way selectively hide.
            return contentIndex == 0
                    && path.endsWith(TIMESTAMP_OR_EMOJI_BUTTONS_ENDS_WITH_PATH)
                    && emojiPickerBufferGroup.check(protobufBufferArray).isFiltered();
        }

        if (matchedGroup == filterChipBar) {
            return aiCommentsSummary.check(protobufBufferArray).isFiltered();
        }

        return true;
    }
}
