package app.revanced.extension.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
final class CommentsFilter extends Filter {

    private final StringFilterGroup chipBar;
    private final ByteArrayFilterGroup aiCommentsSummary;
    
    public CommentsFilter() {
        var chatSummary = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_CHAT_SUMMARY,
                "live_chat_summary_banner.eml"
        );

        chipBar = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_SUMMARY,
                "chip_bar.eml"
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
                "sponsorships_comments_header.eml",
                "sponsorships_comments_footer.eml"
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

        StringFilterGroup timestampButton = new StringFilterGroup(
                Settings.HIDE_COMMENTS_TIMESTAMP_BUTTON,
                "composer_timestamp_button.eml"
        );

        addPathCallbacks(
                channelGuidelines,
                chatSummary,
                chipBar,
                commentsByMembers,
                comments,
                communityGuidelines,
                createAShort,
                previewComment,
                thanksButton,
                timestampButton

        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (matchedGroup == chipBar) {
            // Playlist sort button uses same components and must only filter if the player is opened.
            return PlayerType.getCurrent().isMaximizedOrFullscreen()
                    && aiCommentsSummary.check(protobufBufferArray).isFiltered();
        }

        return true;
    }
}
