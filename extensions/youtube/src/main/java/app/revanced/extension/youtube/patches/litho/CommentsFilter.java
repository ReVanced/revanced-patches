package app.revanced.extension.youtube.patches.litho;

import androidx.annotation.NonNull;

import java.util.List;

import app.revanced.extension.shared.ConversionContext.ContextInterface;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.*;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class CommentsFilter extends Filter {

    private static final String CHIP_BAR_PATH_PREFIX = "chip_bar.e";
    private static final String COMMENT_COMPOSER_PATH = "comment_composer.e";
    private static final String VIDEO_LOCKUP_WITH_ATTACHMENT_PATH = "video_lockup_with_attachment.e";

    private final StringFilterGroup comments;
    private final StringFilterGroup emojiAndTimestampButtons;
    
    public CommentsFilter() {
        var chatSummary = new StringFilterGroup(
                Settings.HIDE_COMMENTS_AI_CHAT_SUMMARY,
                "live_chat_summary_banner.e"
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

        comments = new StringFilterGroup(
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
    public boolean isFiltered(ContextInterface contextInterface,
                              String identifier,
                              String accessibility,
                              String path,
                              byte[] buffer,
                              StringFilterGroup matchedGroup,
                              FilterContentType contentType,
                              int contentIndex) {
        if (matchedGroup == comments) {
            if (path.startsWith(VIDEO_LOCKUP_WITH_ATTACHMENT_PATH)) {
                return Settings.HIDE_COMMENTS_SECTION_IN_HOME_FEED.get();
            }
            return Settings.HIDE_COMMENTS_SECTION.get();
        } else if (matchedGroup == emojiAndTimestampButtons) {
            return path.startsWith(COMMENT_COMPOSER_PATH);
        }

        return true;
    }

    /**
     * Injection point.
     */
    public static void sanitizeCommentsCategoryBar(@NonNull String identifier,
                                                   @NonNull List<Object> treeNodeResultList) {
        try {
            if (Settings.SANITIZE_COMMENTS_CATEGORY_BAR.get()
                    && identifier.startsWith(CHIP_BAR_PATH_PREFIX)
                    // Playlist sort button uses same components and must only filter if the player is opened.
                    && PlayerType.getCurrent().isMaximizedOrFullscreen()
            ) {
                int treeNodeResultListSize = treeNodeResultList.size();
                if (treeNodeResultListSize > 2) {
                    treeNodeResultList.subList(1, treeNodeResultListSize - 1).clear();
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "Failed to sanitize comment category bar", ex);
        }
    }
}
