package app.revanced.extension.youtube.patches.components;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.StringTrieSearch;
import app.revanced.extension.youtube.patches.ChangeHeaderPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class LayoutComponentsFilter extends Filter {
    private static final StringTrieSearch mixPlaylistsExceptions = new StringTrieSearch(
            "V.ED", // Playlist browse id.
            "java.lang.ref.WeakReference"
    );
    private static final ByteArrayFilterGroup mixPlaylistsExceptions2 = new ByteArrayFilterGroup(
            null,
            "cell_description_body"
    );
    private static final ByteArrayFilterGroup mixPlaylists = new ByteArrayFilterGroup(
            null,
            "&list="
    );

    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final StringFilterGroup surveys;
    private final StringFilterGroup notifyMe;
    private final StringFilterGroup singleItemInformationPanel;
    private final StringFilterGroup expandableMetadata;
    private final StringFilterGroup compactChannelBarInner;
    private final StringFilterGroup compactChannelBarInnerButton;
    private final ByteArrayFilterGroup joinMembershipButton;
    private final StringFilterGroup horizontalShelves;
    private final ByteArrayFilterGroup ticketShelf;
    private final StringFilterGroup chipBar;
    private final StringFilterGroup channelProfile;
    private final ByteArrayFilterGroupList channelProfileBuffer;

    public LayoutComponentsFilter() {
        exceptions.addPatterns(
                "home_video_with_context",
                "related_video_with_context",
                "search_video_with_context",
                "comment_thread", // Whitelist comments
                "|comment.", // Whitelist comment replies
                "library_recent_shelf"
        );

        // Identifiers.

        final var chipsShelf = new StringFilterGroup(
                Settings.HIDE_CHIPS_SHELF,
                "chips_shelf"
        );

        addIdentifierCallbacks(
                chipsShelf
        );

        // Paths.

        final var communityPosts = new StringFilterGroup(
                Settings.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper", // may be obsolete and no longer needed.
                "text_post_root.eml",
                "images_post_root.eml",
                "images_post_slim.eml", // may be obsolete and no longer needed.
                "images_post_root_slim.eml",
                "text_post_root_slim.eml",
                "post_base_wrapper_slim.eml",
                "poll_post_root.eml",
                "videos_post_root.eml",
                "post_shelf_slim.eml",
                "videos_post_responsive_root.eml",
                "text_post_responsive_root.eml",
                "poll_post_responsive_root.eml"
        );

        final var subscribersCommunityGuidelines = new StringFilterGroup(
                Settings.HIDE_SUBSCRIBERS_COMMUNITY_GUIDELINES,
                "sponsorships_comments_upsell"
        );

        final var channelMembersShelf = new StringFilterGroup(
                Settings.HIDE_MEMBERS_SHELF,
                "member_recognition_shelf"
        );

        final var compactBanner = new StringFilterGroup(
                Settings.HIDE_COMPACT_BANNER,
                "compact_banner"
        );

        final var subscriptionsChipBar = new StringFilterGroup(
                Settings.HIDE_FILTER_BAR_FEED_IN_FEED,
                "subscriptions_chip_bar"
        );

        chipBar = new StringFilterGroup(
                Settings.HIDE_FILTER_BAR_FEED_IN_HISTORY,
                "chip_bar"
        );

        surveys = new StringFilterGroup(
                Settings.HIDE_SURVEYS,
                "in_feed_survey",
                "slimline_survey",
                "feed_nudge"
        );

        final var medicalPanel = new StringFilterGroup(
                Settings.HIDE_MEDICAL_PANELS,
                "medical_panel"
        );

        final var paidPromotion = new StringFilterGroup(
                Settings.HIDE_PAID_PROMOTION_LABEL,
                "paid_content_overlay"
        );

        final var infoPanel = new StringFilterGroup(
                Settings.HIDE_INFO_PANELS,
                "publisher_transparency_panel"
        );

        singleItemInformationPanel = new StringFilterGroup(
                Settings.HIDE_INFO_PANELS,
                "single_item_information_panel"
        );

        final var latestPosts = new StringFilterGroup(
                Settings.HIDE_LATEST_POSTS,
                "post_shelf"
        );

        final var channelLinksPreview = new StringFilterGroup(
                Settings.HIDE_LINKS_PREVIEW,
                "attribution.eml"
        );

        final var emergencyBox = new StringFilterGroup(
                Settings.HIDE_EMERGENCY_BOX,
                "emergency_onebox"
        );

        // The player audio track button does the exact same function as the audio track flyout menu option.
        // Previously this was a setting to show/hide the player button.
        // But it was decided it's simpler to always hide this button because:
        // - the button is rare
        // - always hiding makes the ReVanced settings simpler and easier to understand
        // - nobody is going to notice the redundant button is always hidden
        final var audioTrackButton = new StringFilterGroup(
                null,
                "multi_feed_icon_button"
        );

        final var artistCard = new StringFilterGroup(
                Settings.HIDE_ARTIST_CARDS,
                "official_card"
        );

        expandableMetadata = new StringFilterGroup(
                Settings.HIDE_EXPANDABLE_CARD,
                "inline_expander"
        );

        final var compactChannelBar = new StringFilterGroup(
                Settings.HIDE_CHANNEL_BAR,
                "compact_channel_bar"
        );

        final var relatedVideos = new StringFilterGroup(
                Settings.HIDE_RELATED_VIDEOS,
                "fullscreen_related_videos"
        );

        final var playables = new StringFilterGroup(
                Settings.HIDE_PLAYABLES,
                "horizontal_gaming_shelf.eml",
                "mini_game_card.eml"
        );

        final var quickActions = new StringFilterGroup(
                Settings.HIDE_QUICK_ACTIONS,
                "quick_actions"
        );

        final var imageShelf = new StringFilterGroup(
                Settings.HIDE_IMAGE_SHELF,
                "image_shelf"
        );

        final var timedReactions = new StringFilterGroup(
                Settings.HIDE_TIMED_REACTIONS,
                "emoji_control_panel",
                "timed_reaction"
        );

        notifyMe = new StringFilterGroup(
                Settings.HIDE_NOTIFY_ME_BUTTON,
                "set_reminder_button"
        );

        compactChannelBarInner = new StringFilterGroup(
                Settings.HIDE_JOIN_MEMBERSHIP_BUTTON,
                "compact_channel_bar_inner"
        );

        compactChannelBarInnerButton = new StringFilterGroup(
                null,
                "|button.eml"
        );

        joinMembershipButton = new ByteArrayFilterGroup(
                null,
                "sponsorships"
        );

        final var channelWatermark = new StringFilterGroup(
                Settings.HIDE_VIDEO_CHANNEL_WATERMARK,
                "featured_channel_watermark_overlay"
        );

        final var forYouShelf = new StringFilterGroup(
                Settings.HIDE_FOR_YOU_SHELF,
                "mixed_content_shelf"
        );

        final var videoRecommendationLabels = new StringFilterGroup(
                Settings.HIDE_VIDEO_RECOMMENDATION_LABELS,
                "endorsement_header_footer.eml"
        );

        channelProfile = new StringFilterGroup(
                null,
                "channel_profile.eml",
                "page_header.eml"
        );
        channelProfileBuffer = new ByteArrayFilterGroupList();
        channelProfileBuffer.addAll(new ByteArrayFilterGroup(
                        Settings.HIDE_VISIT_STORE_BUTTON,
                        "header_store_button"
                ),
                new ByteArrayFilterGroup(
                        Settings.HIDE_VISIT_COMMUNITY_BUTTON,
                        "community_button"
                )
        );

        horizontalShelves = new StringFilterGroup(
                Settings.HIDE_HORIZONTAL_SHELVES,
                "horizontal_video_shelf.eml",
                "horizontal_shelf.eml",
                "horizontal_shelf_inline.eml",
                "horizontal_tile_shelf.eml"
        );

        ticketShelf = new ByteArrayFilterGroup(
                Settings.HIDE_TICKET_SHELF,
                "ticket.eml"
        );

        addPathCallbacks(
                artistCard,
                audioTrackButton,
                channelLinksPreview,
                channelMembersShelf,
                channelProfile,
                channelWatermark,
                chipBar,
                compactBanner,
                compactChannelBar,
                compactChannelBarInner,
                communityPosts,
                emergencyBox,
                expandableMetadata,
                forYouShelf,
                horizontalShelves,
                imageShelf,
                infoPanel,
                latestPosts,
                medicalPanel,
                notifyMe,
                paidPromotion,
                playables,
                quickActions,
                relatedVideos,
                singleItemInformationPanel,
                subscribersCommunityGuidelines,
                subscriptionsChipBar,
                surveys,
                timedReactions,
                videoRecommendationLabels
        );
    }

    @Override
    boolean isFiltered(@Nullable String identifier, String path, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // This identifier is used not only in players but also in search results:
        // https://github.com/ReVanced/revanced-patches/issues/3245
        // Until 2024, medical information panels such as Covid 19 also used this identifier and were shown in the search results.
        // From 2025, the medical information panel is no longer shown in the search results.
        // Therefore, this identifier does not filter when the search bar is activated.
        if (matchedGroup == singleItemInformationPanel) {
            return PlayerType.getCurrent().isMaximizedOrFullscreen() || !NavigationBar.isSearchBarActive();
        }

        // The groups are excluded from the filter due to the exceptions list below.
        // Filter them separately here.
        if (matchedGroup == notifyMe || matchedGroup == surveys || matchedGroup == expandableMetadata) {
            return true;
        }

        if (matchedGroup == channelProfile) {
            return channelProfileBuffer.check(protobufBufferArray).isFiltered();
        }

        if (exceptions.matches(path)) return false; // Exceptions are not filtered.

        if (matchedGroup == compactChannelBarInner) {
            return compactChannelBarInnerButton.check(path).isFiltered()
                    // The filter may be broad, but in the context of a compactChannelBarInnerButton,
                    // it's safe to assume that the button is the only thing that should be hidden.
                    && joinMembershipButton.check(protobufBufferArray).isFiltered();
        }

        if (matchedGroup == horizontalShelves) {
            return contentIndex == 0 && (hideShelves() || ticketShelf.check(protobufBufferArray).isFiltered());
        }

        if (matchedGroup == chipBar) {
            return contentIndex == 0 && NavigationButton.getSelectedNavigationButton() == NavigationButton.LIBRARY;
        }

        return true;
    }

    /**
     * Injection point.
     * Called from a different place then the other filters.
     */
    public static boolean filterMixPlaylists(Object conversionContext, @Nullable final byte[] bytes) {
        try {
            if (!Settings.HIDE_MIX_PLAYLISTS.get()) {
                return false;
            }

            if (bytes == null) {
                Logger.printDebug(() -> "bytes is null");
                return false;
            }

            // Prevent playlist items being hidden, if a mix playlist is present in it.
            if (mixPlaylistsExceptions.matches(conversionContext.toString())) {
                return false;
            }

            // Prevent hiding the description of some videos accidentally.
            if (mixPlaylistsExceptions2.check(bytes).isFiltered()) {
                return false;
            }

            if (mixPlaylists.check(bytes).isFiltered()) {
                Logger.printDebug(() -> "Filtered mix playlist");
                return true;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "filterMixPlaylists failure", ex);
        }

        return false;
    }

    /**
     * Injection point.
     */
    public static boolean showWatermark() {
        return !Settings.HIDE_VIDEO_CHANNEL_WATERMARK.get();
    }

    /**
     * Injection point.
     */
    public static void hideAlbumCard(View view) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_ALBUM_CARDS, view);
    }

    /**
     * Injection point.
     */
    public static void hideCrowdfundingBox(View view) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_CROWDFUNDING_BOX, view);
    }

    /**
     * Injection point.
     */
    public static boolean hideFloatingMicrophoneButton(final boolean original) {
        return original || Settings.HIDE_FLOATING_MICROPHONE_BUTTON.get();
    }

    /**
     * Injection point.
     */
    public static int hideInFeed(final int height) {
        return Settings.HIDE_FILTER_BAR_FEED_IN_FEED.get()
                ? 0
                : height;
    }

    /**
     * Injection point.
     */
    public static int hideInSearch(int height) {
        return Settings.HIDE_FILTER_BAR_FEED_IN_SEARCH.get()
                ? 0
                : height;
    }

    /**
     * Injection point.
     */
    public static void hideInRelatedVideos(View chipView) {
        Utils.hideViewBy0dpUnderCondition(Settings.HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS, chipView);
    }

    private static final boolean HIDE_DOODLES_ENABLED = Settings.HIDE_DOODLES.get();

    /**
     * Injection point.
     */
    public static void setDoodleDrawable(ImageView imageView, Drawable original) {
        Drawable replacement = HIDE_DOODLES_ENABLED
                ? ChangeHeaderPatch.getDrawable(original)
                : original;
        imageView.setImageDrawable(replacement);
    }

    private static final boolean HIDE_SHOW_MORE_BUTTON_ENABLED = Settings.HIDE_SHOW_MORE_BUTTON.get();

    /**
     * Injection point.
     */
    public static void hideShowMoreButton(View view) {
        if (HIDE_SHOW_MORE_BUTTON_ENABLED
                && NavigationBar.isSearchBarActive()
                // Search bar can be active but behind the player.
                && !PlayerType.getCurrent().isMaximizedOrFullscreen()) {
            Utils.hideViewByLayoutParams(view);
        }
    }

    private static boolean hideShelves() {
        // Horizontal shelves are used for music/game links in video descriptions,
        // such as https://youtube.com/watch?v=W8kI1na3S2M
        if (PlayerType.getCurrent().isMaximizedOrFullscreen()) {
            return false;
        }

        // Must check search bar after player type, since search results
        // can be in the background behind an open player.
        if (NavigationBar.isSearchBarActive()) {
            return true;
        }

        // Do not hide if the navigation back button is visible,
        // otherwise the content shelves in the explore/music/courses pages are hidden.
        if (NavigationBar.isBackButtonVisible()) {
            return false;
        }

        // Check navigation button last.
        // Only filter if the library tab is not selected.
        // This check is important as the shelf layout is used for the library tab playlists.
        return NavigationButton.getSelectedNavigationButton() != NavigationButton.LIBRARY;
    }
}
