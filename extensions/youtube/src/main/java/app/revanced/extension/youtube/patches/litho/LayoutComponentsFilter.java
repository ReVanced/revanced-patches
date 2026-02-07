package app.revanced.extension.youtube.patches.litho;

import static app.revanced.extension.youtube.patches.VersionCheckPatch.IS_20_21_OR_GREATER;
import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import app.revanced.extension.shared.ByteTrieSearch;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.StringTrieSearch;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.patches.litho.Filter;
import app.revanced.extension.shared.patches.litho.FilterGroup.ByteArrayFilterGroup;
import app.revanced.extension.shared.patches.litho.FilterGroup.StringFilterGroup;
import app.revanced.extension.shared.patches.litho.FilterGroupList.StringFilterGroupList;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.youtube.patches.ChangeHeaderPatch;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public final class LayoutComponentsFilter extends Filter {
    private static final StringTrieSearch mixPlaylistsContextExceptions = new StringTrieSearch(
            "V.ED", // Playlist browseId.
            "java.lang.ref.WeakReference"
    );
    private static final ByteArrayFilterGroup mixPlaylistsBufferExceptions = new ByteArrayFilterGroup(
            null,
            "cell_description_body",
            "channel_profile"
    );
    private static final ByteArrayFilterGroup mixPlaylists = new ByteArrayFilterGroup(
            null,
            "&list="
    );

    private static final List<String> channelTabFilterStrings;
    private static final List<String> flyoutMenuFilterStrings;

    static {
        channelTabFilterStrings = getFilterStrings(Settings.HIDE_CHANNEL_TAB_FILTER_STRINGS);
        flyoutMenuFilterStrings = getFilterStrings(Settings.HIDE_FEED_FLYOUT_MENU_FILTER_STRINGS);
    }

    private static List<String> getFilterStrings(StringSetting setting) {
        String[] filterArray = setting.get().split("\\n");
        List<String> filters = new ArrayList<>(filterArray.length);

        for (String line : filterArray) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) filters.add(trimmed);
        }

        return filters;
    }

    private final StringTrieSearch exceptions = new StringTrieSearch();
    private final StringFilterGroup communityPosts;
    private final StringFilterGroup surveys;
    private final StringFilterGroup notifyMe;
    private final StringFilterGroup singleItemInformationPanel;
    private final StringFilterGroup expandableMetadata;
    private final StringFilterGroup compactChannelBarInner;
    private final StringFilterGroup compactChannelBarInnerButton;
    private final ByteArrayFilterGroup joinMembershipButton;
    private final StringFilterGroup chipBar;
    private final StringFilterGroup channelProfile;
    private final StringFilterGroupList channelProfileGroupList;
    private final StringFilterGroup horizontalShelves;
    private final ByteArrayFilterGroup playablesBuffer;
    private final ByteArrayFilterGroup ticketShelfBuffer;
    private final ByteArrayFilterGroup playerShoppingShelfBuffer;
    private final ByteTrieSearch descriptionSearch;

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

        final var cellDivider = new StringFilterGroup(
                Settings.HIDE_COMPACT_BANNER,
                // Empty padding and a relic from very old YT versions. Not related to compact banner but included here to avoid adding another setting.
                "cell_divider"
        );

        final var chipsShelf = new StringFilterGroup(
                Settings.HIDE_CHIPS_SHELF,
                "chips_shelf"
        );

        final var liveChatReplay = new StringFilterGroup(
                Settings.HIDE_LIVE_CHAT_REPLAY_BUTTON,
                "live_chat_ep_entrypoint.e"
        );

        addIdentifierCallbacks(
                cellDivider,
                chipsShelf,
                liveChatReplay
        );

        final var visualSpacer = new StringFilterGroup(
                Settings.HIDE_VISUAL_SPACER,
                "cell_divider"
        );

        addIdentifierCallbacks(
                chipsShelf,
                liveChatReplay,
                visualSpacer
        );

        // Paths.

        communityPosts = new StringFilterGroup(
                Settings.HIDE_COMMUNITY_POSTS,
                "post_base_wrapper", // may be obsolete and no longer needed.
                "text_post_root.e",
                "images_post_root.e",
                "images_post_slim.e", // may be obsolete and no longer needed.
                "images_post_root_slim.e",
                "text_post_root_slim.e",
                "post_base_wrapper_slim.e",
                "poll_post_root.e",
                "videos_post_root.e",
                "post_shelf_slim.e",
                "videos_post_responsive_root.e",
                "text_post_responsive_root.e",
                "poll_post_responsive_root.e",
                "shared_post_root.e"
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

        final var subscribedChannelsBar = new StringFilterGroup(
                Settings.HIDE_SUBSCRIBED_CHANNELS_BAR,
                "subscriptions_channel_bar"
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
                "attribution.e"
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
                "horizontal_gaming_shelf.e",
                "mini_game_card.e"
        );

        // Playable horizontal shelf header.
        playablesBuffer = new ByteArrayFilterGroup(
                null,
                "FEmini_app_destination"
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
                "|button.e"
        );

        joinMembershipButton = new ByteArrayFilterGroup(
                null,
                "sponsorships"
        );

        final var crowdfundingBox = new StringFilterGroup(
                Settings.HIDE_CROWDFUNDING_BOX,
                "donation_shelf"
        );

        final var channelWatermark = new StringFilterGroup(
                Settings.HIDE_CHANNEL_WATERMARK,
                "featured_channel_watermark_overlay"
        );

        final var forYouShelf = new StringFilterGroup(
                Settings.HIDE_FOR_YOU_SHELF,
                "mixed_content_shelf"
        );

        final var videoRecommendationLabels = new StringFilterGroup(
                Settings.HIDE_VIDEO_RECOMMENDATION_LABELS,
                "endorsement_header_footer.e"
        );

        final var videoTitle = new StringFilterGroup(
                Settings.HIDE_VIDEO_TITLE,
                "player_overlay_video_heading.e"
        );

        final var webLinkPanel = new StringFilterGroup(
                Settings.HIDE_WEB_SEARCH_RESULTS,
                "web_link_panel",
                "web_result_panel"
        );

        channelProfile = new StringFilterGroup(
                null,
                "channel_profile.e",
                "page_header.e"
        );
        channelProfileGroupList = new StringFilterGroupList();
        channelProfileGroupList.addAll(new StringFilterGroup(
                        Settings.HIDE_COMMUNITY_BUTTON,
                        "community_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_JOIN_BUTTON,
                        "sponsor_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_STORE_BUTTON,
                        "header_store_button"
                ),
                new StringFilterGroup(
                        Settings.HIDE_SUBSCRIBE_BUTTON_IN_CHANNEL_PAGE,
                        "subscribe_button"
                )
        );

        horizontalShelves = new StringFilterGroup(
                null, // Setting is checked in isFiltered()
                "horizontal_video_shelf.e",
                "horizontal_shelf.e",
                "horizontal_shelf_inline.e",
                "horizontal_tile_shelf.e"
        );

        ticketShelfBuffer = new ByteArrayFilterGroup(
                null,
                "ticket_item.e"
        );

        playerShoppingShelfBuffer = new ByteArrayFilterGroup(
                null,
                "shopping_item_card_list"
        );

        // Work around for unique situation where filtering is based on the setting,
        // but it must not fall over to other filters if the setting is _not_ enabled.
        // This is only needed for the horizontal shelf that is used so extensively everywhere.
        descriptionSearch = new ByteTrieSearch();
        List.of(
                new Pair<>(Settings.HIDE_FEATURED_PLACES_SECTION, "yt_fill_star"),
                new Pair<>(Settings.HIDE_FEATURED_PLACES_SECTION, "yt_fill_experimental_star"),
                new Pair<>(Settings.HIDE_GAMING_SECTION, "yt_outline_gaming"),
                new Pair<>(Settings.HIDE_GAMING_SECTION, "yt_outline_experimental_gaming"),
                new Pair<>(Settings.HIDE_MUSIC_SECTION, "yt_outline_audio"),
                new Pair<>(Settings.HIDE_MUSIC_SECTION, "yt_outline_experimental_audio"),
                new Pair<>(Settings.HIDE_QUIZZES_SECTION, "post_base_wrapper_slim"),
                // May no longer work on v20.31+, even though the component is still there.
                new Pair<>(Settings.HIDE_ATTRIBUTES_SECTION, "cell_video_attribute")
        ).forEach(pair -> {
                    BooleanSetting setting = pair.first;
                    descriptionSearch.addPattern(pair.second.getBytes(StandardCharsets.UTF_8),
                            (textSearched, matchedStartIndex, matchedLength, callbackParameter) -> {
                                //noinspection unchecked
                                AtomicReference<Boolean> hide = (AtomicReference<Boolean>) callbackParameter;
                                hide.set(setting.get());
                                return true;
                            }
                    );
                }
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
                crowdfundingBox,
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
                subscribedChannelsBar,
                subscribersCommunityGuidelines,
                subscriptionsChipBar,
                surveys,
                timedReactions,
                videoTitle,
                videoRecommendationLabels,
                webLinkPanel
        );
    }

    @Override
    public boolean isFiltered(String identifier, String accessibility, String path, byte[] buffer,
                              StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        // This identifier is used not only in players but also in search results:
        // https://github.com/ReVanced/revanced-patches/issues/3245
        // Until 2024, medical information panels such as Covid-19 also used this identifier and were shown in the search results.
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
            return channelProfileGroupList.check(accessibility).isFiltered();
        }

        if (matchedGroup == communityPosts
                && NavigationBar.isBackButtonVisible()
                && !NavigationBar.isSearchBarActive()
                && PlayerType.getCurrent() != PlayerType.WATCH_WHILE_MAXIMIZED) {
            // Allow community posts on channel profile page,
            // or if viewing an individual channel in the feed.
            return false;
        }

        if (exceptions.matches(path)) return false; // Exceptions are not filtered.

        if (matchedGroup == compactChannelBarInner) {
            return compactChannelBarInnerButton.check(path).isFiltered()
                    // The filter may be broad, but in the context of a compactChannelBarInnerButton,
                    // it's safe to assume that the button is the only thing that should be hidden.
                    && joinMembershipButton.check(buffer).isFiltered();
        }

        // Horizontal shelves are used everywhere in the app. And to prevent the generic "hide shelves"
        // from incorrectly hiding other stuff that has its own hide filters,
        // the more specific shelf filters must check first _and_ they must halt falling over
        // to other filters if the buffer matches but the setting is off.
        if (matchedGroup == horizontalShelves) {
            if (contentIndex != 0) return false;

            AtomicReference<Boolean> descriptionFilterResult = new AtomicReference<>(null);
            if (descriptionSearch.matches(buffer, descriptionFilterResult)) {
                return descriptionFilterResult.get();
            }

            // Check if others are off before searching.
            final boolean hideShelves = Settings.HIDE_HORIZONTAL_SHELVES.get();
            final boolean hideTickets = Settings.HIDE_TICKET_SHELF.get();
            final boolean hidePlayables = Settings.HIDE_PLAYABLES.get();
            final boolean hidePlayerShoppingShelf = Settings.HIDE_CREATOR_STORE_SHELF.get();
            if (!hideShelves && !hideTickets && !hidePlayables && !hidePlayerShoppingShelf)
                return false;

            if (ticketShelfBuffer.check(buffer).isFiltered()) return hideTickets;
            if (playablesBuffer.check(buffer).isFiltered()) return hidePlayables;
            if (playerShoppingShelfBuffer.check(buffer).isFiltered())
                return hidePlayerShoppingShelf;

            // 20.31+ when exiting fullscreen after watching for a while or when resuming the app,
            // then sometimes the buffer isn't correct and the player shopping shelf is shown.
            // If filtering reaches this point then there are no more shelves that could be in the player.
            // If shopping shelves are set to hidden and the player is active, then assume
            // it's the shopping shelf.
            if (hidePlayerShoppingShelf) {
                PlayerType type = PlayerType.getCurrent();
                if (type == PlayerType.WATCH_WHILE_MAXIMIZED || type == PlayerType.WATCH_WHILE_FULLSCREEN
                        || type == PlayerType.WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN) {
                    return true;
                }
            }

            return hideShelves && hideShelves();
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
    public static boolean filterMixPlaylists(Object conversionContext, @Nullable byte[] buffer) {
        // Edit: This hook may no longer be needed, and mix playlist filtering
        //       might be possible using the existing litho filters.
        try {
            if (!Settings.HIDE_MIX_PLAYLISTS.get()) {
                return false;
            }

            if (buffer == null) {
                Logger.printDebug(() -> "buffer is null");
                return false;
            }

            if (mixPlaylists.check(buffer).isFiltered()
                    // Prevent hiding the description of some videos accidentally.
                    && !mixPlaylistsBufferExceptions.check(buffer).isFiltered()
                    // Prevent playlist items being hidden, if a mix playlist is present in it.
                    // Check last since it requires creating a context string.
                    //
                    // FIXME: The conversion context passed in does not always generate a valid toString.
                    //        This string check may no longer be needed, or the patch may be broken.
                    && !mixPlaylistsContextExceptions.matches(conversionContext.toString())) {
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
        return !Settings.HIDE_CHANNEL_WATERMARK.get();
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
    public static void hideLatestVideosButton(View view) {
        Utils.hideViewUnderCondition(Settings.HIDE_LATEST_VIDEOS_BUTTON.get(), view);
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

    private static final boolean HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS_ENABLED
            = Settings.HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS.get();

    /**
     * Injection point.
     */
    public static void hideInRelatedVideos(View chipView) {
        // Cannot use 0dp hide with later targets, otherwise the suggested videos
        // can be shown in full screen mode.
        // This behavior may also be present in earlier app targets.
        if (IS_20_21_OR_GREATER) {
            // FIXME: The filter bar is still briefly shown when dragging the suggested videos
            //        below the video player.
            Utils.hideViewUnderCondition(HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS_ENABLED, chipView);
        } else {
            Utils.hideViewBy0dpUnderCondition(HIDE_FILTER_BAR_FEED_IN_RELATED_VIDEOS_ENABLED, chipView);
        }
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

    private static final FrameLayout.LayoutParams EMPTY_LAYOUT_PARAMS = new FrameLayout.LayoutParams(0, 0);
    private static final boolean HIDE_SHOW_MORE_BUTTON_ENABLED = Settings.HIDE_SHOW_MORE_BUTTON.get();

    /**
     * The ShowMoreButton should not always be hidden.
     * According to the preference summary, only the ShowMoreButton in search results is hidden.
     * Since the ShowMoreButton should be visible on other pages, such as channels,
     * the original values of the Views are saved in fields.
     */
    private static FrameLayout.LayoutParams cachedLayoutParams;
    private static int cachedButtonContainerMinimumHeight = -1;
    private static int cachedPlaceHolderMinimumHeight = -1;
    private static int cachedRootViewMinimumHeight = -1;

    /**
     * Injection point.
     */
    public static void hideShowMoreButton(View view, View buttonContainer, TextView textView) {
        if (HIDE_SHOW_MORE_BUTTON_ENABLED
                && view instanceof ViewGroup rootView
                && buttonContainer != null
                && textView != null
                && buttonContainer.getLayoutParams() instanceof FrameLayout.LayoutParams lp
        ) {
            View placeHolder = rootView.getChildAt(0);

            // For some users, ShowMoreButton has a PlaceHolder ViewGroup (A/B tests).
            // When a PlaceHolder is present, a different method is used to hide or show the ViewGroup.
            boolean hasPlaceHolder = placeHolder instanceof FrameLayout;

            // Only in search results, the content description of RootView and the text of TextView match.
            // Hide ShowMoreButton in search results, but show ShowMoreButton in other pages (e.g. channels).
            boolean isSearchResults = TextUtils.equals(rootView.getContentDescription(), textView.getText());

            if (hasPlaceHolder) {
                hideShowMoreButtonWithPlaceHolder(placeHolder, isSearchResults);
            } else {
                hideShowMoreButtonWithOutPlaceHolder(buttonContainer, lp, isSearchResults);
            }

            if (cachedRootViewMinimumHeight == -1) {
                cachedRootViewMinimumHeight = rootView.getMinimumHeight();
            }

            if (isSearchResults) {
                rootView.setMinimumHeight(0);
                rootView.setVisibility(View.GONE);
            } else {
                rootView.setMinimumHeight(cachedRootViewMinimumHeight);
                rootView.setVisibility(View.VISIBLE);
            }
        }
    }

    private static void hideShowMoreButtonWithPlaceHolder(View placeHolder, boolean isSearchResults) {
        if (cachedPlaceHolderMinimumHeight == -1) {
            cachedPlaceHolderMinimumHeight = placeHolder.getMinimumHeight();
        }

        if (isSearchResults) {
            placeHolder.setMinimumHeight(0);
            placeHolder.setVisibility(View.GONE);
        } else {
            placeHolder.setMinimumHeight(cachedPlaceHolderMinimumHeight);
            placeHolder.setVisibility(View.VISIBLE);
        }
    }

    private static void hideShowMoreButtonWithOutPlaceHolder(View buttonContainer, FrameLayout.LayoutParams lp,
                                                             boolean isSearchResults) {
        if (cachedButtonContainerMinimumHeight == -1) {
            cachedButtonContainerMinimumHeight = buttonContainer.getMinimumHeight();
        }

        if (cachedLayoutParams == null) {
            cachedLayoutParams = lp;
        }

        if (isSearchResults) {
            buttonContainer.setMinimumHeight(0);
            buttonContainer.setLayoutParams(EMPTY_LAYOUT_PARAMS);
            buttonContainer.setVisibility(View.GONE);
        } else {
            buttonContainer.setMinimumHeight(cachedButtonContainerMinimumHeight);
            buttonContainer.setLayoutParams(cachedLayoutParams);
            buttonContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Injection point.
     */
    public static void hideSubscribedChannelsBar(View view) {
        Utils.hideViewByRemovingFromParentUnderCondition(Settings.HIDE_SUBSCRIBED_CHANNELS_BAR, view);
    }

    /**
     * Injection point.
     */
    public static int hideSubscribedChannelsBar(int original) {
        return Settings.HIDE_SUBSCRIBED_CHANNELS_BAR.get()
                ? 0
                : original;
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

    /**
     * Injection point.
     */
    public static SpannableString modifyFeedSubtitleSpan(SpannableString original, float truncationDimension) {
        try {
            final boolean hideViewCount = Settings.HIDE_VIEW_COUNT.get();
            final boolean hideUploadTime = Settings.HIDE_UPLOAD_TIME.get();
            if (!hideViewCount && !hideUploadTime) {
                return original;
            }

            // Applies only for these specific dimensions.
            if (truncationDimension == 16f || truncationDimension == 42f) {
                String delimiter = " · ";
                final int delimiterLength = delimiter.length();

                // Index includes the starting delimiter.
                final int viewCountStartIndex = TextUtils.indexOf(original, delimiter);
                if (viewCountStartIndex < 0) {
                    return original;
                }

                final int uploadTimeStartIndex = TextUtils.indexOf(original, delimiter,
                        viewCountStartIndex + delimiterLength);
                if (uploadTimeStartIndex < 0) {
                    return original;
                }

                // Ensure there is exactly 2 delimiters.
                if (TextUtils.indexOf(original, delimiter,
                        uploadTimeStartIndex + delimiterLength) >= 0) {
                    return original;
                }

                // Make a mutable copy that keeps existing span styling.
                SpannableStringBuilder builder = new SpannableStringBuilder(original);

                // Remove the sections.
                if (hideUploadTime) {
                    builder.delete(uploadTimeStartIndex, original.length());
                }

                if (hideViewCount) {
                    builder.delete(viewCountStartIndex, uploadTimeStartIndex);
                }

                SpannableString replacement = new SpannableString(builder);
                Logger.printDebug(() -> "Replacing feed subtitle span: " + original + " with: " + replacement);

                return replacement;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "modifyFeedSubtitleSpan failure", ex);
        }

        return original;
    }

    /**
     *
     * Injection point.
     * <p>
     * Hide feed flyout menu for phone
     *
     * @param menuTitleCharSequence menu title
     */
    @Nullable
    public static CharSequence hideFlyoutMenu(@Nullable CharSequence menuTitleCharSequence) {
        if (menuTitleCharSequence == null || !Settings.HIDE_FEED_FLYOUT_MENU.get()
                || flyoutMenuFilterStrings.isEmpty()) {
            return menuTitleCharSequence;
        }

        String menuTitleString = menuTitleCharSequence.toString();

        for (String filter : flyoutMenuFilterStrings) {
            if (menuTitleString.equalsIgnoreCase(filter)) {
                return null;
            }
        }

        return menuTitleCharSequence;
    }

    /**
     * Injection point.
     * <p>
     * hide feed flyout panel for tablet
     *
     * @param menuTextView          flyout text view
     * @param menuTitleCharSequence raw text
     */
    public static void hideFlyoutMenu(TextView menuTextView, CharSequence menuTitleCharSequence) {
        if (menuTitleCharSequence == null || !Settings.HIDE_FEED_FLYOUT_MENU.get()
                || flyoutMenuFilterStrings.isEmpty()
                || !(menuTextView.getParent() instanceof View parentView)) {
            return;
        }

        String menuTitleString = menuTitleCharSequence.toString();

        for (String filter : flyoutMenuFilterStrings) {
            if (menuTitleString.equalsIgnoreCase(filter)) {
                Utils.hideViewByLayoutParams(parentView);
            }
        }
    }

    /**
     *
     * Injection point.
     * <p>
     * Rather than simply hiding the channel tab view, completely remove the channel tab from the list.
     * If a channel tab is removed from the list, users will not be able to open it by swiping.
     *
     * @param channelTabText Text assigned to the channel tab, such as "Shorts", "Playlists",
     *                       "Community", "Store". This text follows the user's language.
     * @return Whether to remove the channel tab from the list.
     */
    public static boolean hideChannelTab(@Nullable String channelTabText) {
        if (channelTabText == null || !Settings.HIDE_CHANNEL_TAB.get()
                || channelTabFilterStrings.isEmpty()) {
            return false;
        }

        for (String filter : channelTabFilterStrings) {
            if (channelTabText.equalsIgnoreCase(filter)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Injection point.
     *
     * @param typedString   Keywords typed in the search bar.
     * @return              Whether the setting is enabled and the typed string is empty.
     */
    public static boolean hideYouMayLikeSection(String typedString) {
        return Settings.HIDE_YOU_MAY_LIKE_SECTION.get()
                // The 'You may like' section is only visible when no search terms are entered.
                // To avoid unnecessary collection traversals, filtering is performed only when the typedString is empty.
                && TextUtils.isEmpty(typedString);
    }

    /**
     * Injection point.
     *
     * @param searchTerm    This class contains information related to search terms.
     *                      The {@code toString()} method of this class overrides the search term.
     * @param endpoint      Endpoint related with the search term.
     *                      For search history, this value is:
     *                      '/complete/deleteitems?client=youtube-android-pb&delq=${searchTerm}&deltok=${token}'.
     *                      For search suggestions, this value is null or empty.
     * @return              Whether search term is a search history or not.
     */
    public static boolean isSearchHistory(Object searchTerm, String endpoint) {
        boolean isSearchHistory = endpoint != null && endpoint.contains("/delete");
        if (!isSearchHistory) {
            Logger.printDebug(() -> "Remove search suggestion: " + searchTerm);
        }
        return isSearchHistory;
    }
}
