package app.revanced.extension.youtube.patches.components;

import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.*;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class CommentsFilter extends Filter {

    private static final String COMMENT_COMPOSER_PATH = "comment_composer.e";

    private final StringFilterGroup chipBar;
    private final ByteArrayFilterGroup aiCommentsSummary;
    private final StringFilterGroup emojiAndTimestampButtons;
    
    public CommentsFilter() {
        var chatSummary = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_CHAT_SUMMARY,
                "live_chat_summary_banner.e"
        );

        chipBar = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_SUMMARY,
                "chip_bar.e"
        );

        aiCommentsSummary = new ByteArrayFilterGroup(
                null,
                "yt_fill_spark_"
        );

        var channelGuidelines = new StringFilterGroup(
                Settings.HIDE_COMMENTS_CHANNEL_GUIDELINES,
                "channel_guidelines_entry_banner"
        );

        var commentsByMembers = new StringFilterGroup(
                Settings.HIDE_COMMENTS_BY_MEMBERS_HEADER,
                "sponsorships_comments_header.e",
                "sponsorships_comments_footer.e"
        );

        var comments = new StringFilterGroup(
                Settings.HIDE_COMMENTS_SECTION,
                "video_metadata_carousel",
                "_comments"
        );

        var communityGuidelines = new StringFilterGroup(
                Settings.HIDE_COMMENTS_COMMUNITY_GUIDELINES,
                "community_guidelines"
        );

        var createAShort = new StringFilterGroup(
                Settings.HIDE_COMMENTS_CREATE_A_SHORT_BUTTON,
                "composer_short_creation_button.e"
        );

        emojiAndTimestampButtons = new StringFilterGroup(
                Settings.HIDE_COMMENTS_EMOJI_AND_TIMESTAMP_BUTTONS,
                "|CellType|ContainerType|ContainerType|ContainerType|ContainerType|ContainerType|"
        );

        var previewComment = new StringFilterGroup(
                Settings.HIDE_COMMENTS_PREVIEW_COMMENT,
                "|carousel_item",
                "comments_entry_point_teaser",
                "comments_entry_point_simplebox"
        );

        var thanksButton = new StringFilterGroup(
                Settings.HIDE_COMMENTS_THANKS_BUTTON,
                "super_thanks_button.e"
        );

        addPathCallbacks(
                channelGuidelines,
                chatSummary,
                chipBar,
                commentsByMembers,
                comments,
                communityGuidelines,
                createAShort,
                emojiAndTimestampButtons,
                previewComment,
                thanksButton

        );
    }

    @Override
    public boolean isFiltered(String identifier, String path, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == chipBar) {
            // Playlist sort button uses same components and must only filter if the player is opened.
            return PlayerType.getCurrent().isMaximizedOrFullscreen()
                    && aiCommentsSummary.check(buffer).isFiltered();
        }

        if (matchedGroup == emojiAndTimestampButtons) {
            return path.startsWith(COMMENT_COMPOSER_PATH);
        }

        return true;
    }
}
