package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
final class CommentsFilter extends Filter {

    private static final String TIMESTAMP_OR_EMOJI_BUTTONS_ENDS_WITH_PATH
            = "|CellType|ContainerType|ContainerType|ContainerType|ContainerType|ContainerType|";

    private final StringFilterGroup commentComposer;
    private final ByteArrayFilterGroup emojiPickerBufferGroup;
    
    public CommentsFilter() {
        var comments = new StringFilterGroup(
                Settings.HIDE_COMMENTS_SECTION,
                "video_metadata_carousel",
                "_comments"
        );

        var previewComment = new StringFilterGroup(
                Settings.HIDE_PREVIEW_COMMENT,
                "|carousel_item",
                "comments_entry_point_teaser",
                "comments_entry_point_simplebox"
        );

        commentComposer = new StringFilterGroup(
                Settings.HIDE_COMMENT_TIMESTAMP_AND_EMOJI_BUTTONS,
                "comment_composer.eml"
        );

        emojiPickerBufferGroup = new ByteArrayFilterGroup(
                null,
                "id.comment.quick_emoji.button"
        );

        addPathCallbacks(
                comments,
                previewComment,
                commentComposer
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == commentComposer) {
            // To completely hide the emoji buttons (and leave no empty space), the timestamp button is
            // also hidden because the buffer is exactly the same and there's no way selectively hide.
            if (contentIndex == 0
                    && path.endsWith(TIMESTAMP_OR_EMOJI_BUTTONS_ENDS_WITH_PATH)
                    && emojiPickerBufferGroup.check(protobufBufferArray).isFiltered()) {
                return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
            }

            return false;
        }

        return super.isFiltered(identifier, path, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }
}
