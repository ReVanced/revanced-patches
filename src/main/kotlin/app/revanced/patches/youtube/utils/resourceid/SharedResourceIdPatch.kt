package app.revanced.patches.youtube.utils.resourceid

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.mapping.ResourceMappingPatch
import app.revanced.patches.shared.patch.mapping.ResourceType
import app.revanced.patches.shared.patch.mapping.ResourceType.COLOR
import app.revanced.patches.shared.patch.mapping.ResourceType.DIMEN
import app.revanced.patches.shared.patch.mapping.ResourceType.DRAWABLE
import app.revanced.patches.shared.patch.mapping.ResourceType.ID
import app.revanced.patches.shared.patch.mapping.ResourceType.LAYOUT
import app.revanced.patches.shared.patch.mapping.ResourceType.STRING

@Patch(dependencies = [ResourceMappingPatch::class])
object SharedResourceIdPatch : ResourcePatch() {
    var AccountSwitcherAccessibility: Long = -1
    var ActionBarRingo: Long = -1
    var AdAttribution: Long = -1
    var Appearance: Long = -1
    var AppRelatedEndScreenResults: Long = -1
    var AutoNavPreviewStub: Long = -1
    var AutoNavToggle: Long = -1
    var BackgroundCategory: Long = -1
    var Bar: Long = -1
    var BarContainerHeight: Long = -1
    var BottomSheetFooterText: Long = -1
    var BottomUiContainerStub: Long = -1
    var ChannelListSubMenu: Long = -1
    var CompactLink: Long = -1
    var CompactListItem: Long = -1
    var ControlsLayoutStub: Long = -1
    var CoreContainer: Long = -1
    var DarkSplashAnimation: Long = -1
    var DislikeButton: Long = -1
    var DonationCompanion: Long = -1
    var EasySeekEduContainer: Long = -1
    var EditSettingsAction: Long = -1
    var EndScreenElementLayoutCircle: Long = -1
    var EndScreenElementLayoutIcon: Long = -1
    var EndScreenElementLayoutVideo: Long = -1
    var EmojiPickerIcon: Long = -1
    var ExpandButtonDown: Long = -1
    var Fab: Long = -1
    var FilterBarHeight: Long = -1
    var FloatyBarTopMargin: Long = -1
    var FullScreenEngagementOverlay: Long = -1
    var FullScreenEngagementPanel: Long = -1
    var HorizontalCardList: Long = -1
    var ImageOnlyTab: Long = -1
    var InlineTimeBarColorizedBarPlayedColorDark: Long = -1
    var InlineTimeBarPlayedNotHighlightedColor: Long = -1
    var InsetOverlayViewLayout: Long = -1
    var LiveChatButton: Long = -1
    var MenuItemView: Long = -1
    var MusicAppDeeplinkButtonView: Long = -1
    var PosterArtWidthDefault: Long = -1
    var QualityAuto: Long = -1
    var QuickActionsElementContainer: Long = -1
    var ReelDynRemix: Long = -1
    var ReelDynShare: Long = -1
    var ReelForcedMuteButton: Long = -1
    var ReelPivotButton: Long = -1
    var ReelPlayerBadge: Long = -1
    var ReelPlayerBadge2: Long = -1
    var ReelPlayerFooter: Long = -1
    var ReelPlayerInfoPanel: Long = -1
    var ReelPlayerPausedStateButton: Long = -1
    var ReelRightDislikeIcon: Long = -1
    var ReelRightLikeIcon: Long = -1
    var ReelTimeBarPlayedColor: Long = -1
    var RelatedChipCloudMargin: Long = -1
    var RightComment: Long = -1
    var ScrimOverlay: Long = -1
    var Scrubbing: Long = -1
    var SeekUndoEduOverlayStub: Long = -1
    var SettingsBooleanTimeRangeDialog: Long = -1
    var SubtitleMenuSettingsFooterInfo: Long = -1
    var SuggestedAction: Long = -1
    var TabsBarTextTabView: Long = -1
    var ToolTipContentView: Long = -1
    var TotalTime: Long = -1
    var TouchArea: Long = -1
    var VideoQualityBottomSheet: Long = -1
    var VideoZoomIndicatorLayout: Long = -1
    var YoutubeControlsOverlay: Long = -1
    var YoutubeControlsOverlaySubtitleButton: Long = -1
    var YtOutlineArrowTimeBlack: Long = -1
    var YtOutlineFireBlack: Long = -1
    var YtOutlineSearchBlack: Long = -1

    override fun execute(context: ResourceContext) {

        fun find(resourceType: ResourceType, resourceName: String) = ResourceMappingPatch
            .resourceMappings
            .find { it.type == resourceType.value && it.name == resourceName }?.id
            ?: -1

        AccountSwitcherAccessibility = find(STRING, "account_switcher_accessibility_label")
        ActionBarRingo = find(LAYOUT, "action_bar_ringo")
        AdAttribution = find(ID, "ad_attribution")
        Appearance = find(STRING, "app_theme_appearance_dark")
        AppRelatedEndScreenResults = find(LAYOUT, "app_related_endscreen_results")
        AutoNavPreviewStub = find(ID, "autonav_preview_stub")
        AutoNavToggle = find(ID, "autonav_toggle")
        BackgroundCategory = find(STRING, "pref_background_and_offline_category")
        Bar = find(LAYOUT, "bar")
        BarContainerHeight = find(DIMEN, "bar_container_height")
        BottomSheetFooterText = find(ID, "bottom_sheet_footer_text")
        BottomUiContainerStub = find(ID, "bottom_ui_container_stub")
        ChannelListSubMenu = find(LAYOUT, "channel_list_sub_menu")
        CompactLink = find(LAYOUT, "compact_link")
        CompactListItem = find(LAYOUT, "compact_list_item")
        ControlsLayoutStub = find(ID, "controls_layout_stub")
        CoreContainer = find(ID, "core_container")
        DarkSplashAnimation = find(ID, "dark_splash_animation")
        DislikeButton = find(ID, "dislike_button")
        DonationCompanion = find(LAYOUT, "donation_companion")
        EasySeekEduContainer = find(ID, "easy_seek_edu_container")
        EditSettingsAction = find(STRING, "edit_settings_action")
        EndScreenElementLayoutCircle = find(LAYOUT, "endscreen_element_layout_circle")
        EndScreenElementLayoutIcon = find(LAYOUT, "endscreen_element_layout_icon")
        EndScreenElementLayoutVideo = find(LAYOUT, "endscreen_element_layout_video")
        EmojiPickerIcon = find(ID, "emoji_picker_icon")
        ExpandButtonDown = find(LAYOUT, "expand_button_down")
        Fab = find(ID, "fab")
        FilterBarHeight = find(DIMEN, "filter_bar_height")
        FloatyBarTopMargin = find(DIMEN, "floaty_bar_button_top_margin")
        FullScreenEngagementOverlay = find(LAYOUT, "fullscreen_engagement_overlay")
        FullScreenEngagementPanel = find(ID, "fullscreen_engagement_panel_holder")
        HorizontalCardList = find(LAYOUT, "horizontal_card_list")
        ImageOnlyTab = find(LAYOUT, "image_only_tab")
        InlineTimeBarColorizedBarPlayedColorDark =
            find(COLOR, "inline_time_bar_colorized_bar_played_color_dark")
        InlineTimeBarPlayedNotHighlightedColor =
            find(COLOR, "inline_time_bar_played_not_highlighted_color")
        InsetOverlayViewLayout = find(ID, "inset_overlay_view_layout")
        LiveChatButton = find(ID, "live_chat_overlay_button")
        MenuItemView = find(ID, "menu_item_view")
        MusicAppDeeplinkButtonView = find(ID, "music_app_deeplink_button_view")
        PosterArtWidthDefault = find(DIMEN, "poster_art_width_default")
        QualityAuto = find(STRING, "quality_auto")
        QuickActionsElementContainer = find(ID, "quick_actions_element_container")
        ReelDynRemix = find(ID, "reel_dyn_remix")
        ReelDynShare = find(ID, "reel_dyn_share")
        ReelForcedMuteButton = find(ID, "reel_player_forced_mute_button")
        ReelPivotButton = find(ID, "reel_pivot_button")
        ReelPlayerBadge = find(ID, "reel_player_badge")
        ReelPlayerBadge2 = find(ID, "reel_player_badge2")
        ReelPlayerFooter = find(LAYOUT, "reel_player_dyn_footer_vert_stories3")
        ReelPlayerInfoPanel = find(ID, "reel_player_info_panel")
        ReelPlayerPausedStateButton = find(ID, "reel_player_paused_state_buttons")
        ReelRightDislikeIcon = find(DRAWABLE, "reel_right_dislike_icon")
        ReelRightLikeIcon = find(DRAWABLE, "reel_right_like_icon")
        ReelTimeBarPlayedColor = find(COLOR, "reel_time_bar_played_color")
        RelatedChipCloudMargin = find(LAYOUT, "related_chip_cloud_reduced_margins")
        RightComment = find(DRAWABLE, "ic_right_comment_32c")
        ScrimOverlay = find(ID, "scrim_overlay")
        Scrubbing = find(DIMEN, "vertical_touch_offset_to_enter_fine_scrubbing")
        SeekUndoEduOverlayStub = find(ID, "seek_undo_edu_overlay_stub")
        SettingsBooleanTimeRangeDialog = find(LAYOUT, "setting_boolean_time_range_dialog")
        SubtitleMenuSettingsFooterInfo = find(STRING, "subtitle_menu_settings_footer_info")
        SuggestedAction = find(LAYOUT, "suggested_action")
        TabsBarTextTabView = find(ID, "tabs_bar_text_tab_view")
        ToolTipContentView = find(LAYOUT, "tooltip_content_view")
        TotalTime = find(STRING, "total_time")
        TouchArea = find(ID, "touch_area")
        VideoQualityBottomSheet = find(LAYOUT, "video_quality_bottom_sheet_list_fragment_title")
        VideoZoomIndicatorLayout = find(ID, "video_zoom_indicator_layout")
        YoutubeControlsOverlay = find(ID, "youtube_controls_overlay")
        YoutubeControlsOverlaySubtitleButton = find(LAYOUT, "youtube_controls_overlay_subtitle_button")
        YtOutlineArrowTimeBlack = find(DRAWABLE, "yt_outline_arrow_time_black_24")
        YtOutlineFireBlack = find(DRAWABLE, "yt_outline_fire_black_24")
        YtOutlineSearchBlack = find(DRAWABLE, "yt_outline_search_black_24")

    }
}
