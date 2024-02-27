package app.revanced.patches.music.utils.resourceid

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.mapping.ResourceMappingPatch
import app.revanced.patches.shared.patch.mapping.ResourceType
import app.revanced.patches.shared.patch.mapping.ResourceType.BOOL
import app.revanced.patches.shared.patch.mapping.ResourceType.COLOR
import app.revanced.patches.shared.patch.mapping.ResourceType.DIMEN
import app.revanced.patches.shared.patch.mapping.ResourceType.ID
import app.revanced.patches.shared.patch.mapping.ResourceType.LAYOUT
import app.revanced.patches.shared.patch.mapping.ResourceType.STRING
import app.revanced.patches.shared.patch.mapping.ResourceType.STYLE

@Patch(dependencies = [ResourceMappingPatch::class])
object SharedResourceIdPatch : ResourcePatch() {
    var AccountSwitcherAccessibility: Long = -1
    var ButtonContainer: Long = -1
    var ButtonIconPaddingMedium: Long = -1
    var ChipCloud: Long = -1
    var ColorGrey: Long = -1
    var DesignBottomSheetDialog: Long = -1
    var DialogSolid: Long = -1
    var FloatingLayout: Long = -1
    var HistoryMenuItem: Long = -1
    var InlineTimeBarAdBreakMarkerColor: Long = -1
    var IsTablet: Long = -1
    var LikeDislikeContainer: Long = -1
    var MenuEntry: Long = -1
    var MiniPlayerMdxPlaying: Long = -1
    var MusicMenuLikeButtons: Long = -1
    var MusicNotifierShelf: Long = -1
    var MusicTastebuilderShelf: Long = -1
    var NamesInactiveAccountThumbnailSize: Long = -1
    var OfflineSettingsMenuItem: Long = -1
    var PlayerCastMediaRouteButton: Long = -1
    var PlayerOverlayChip: Long = -1
    var PrivacyTosFooter: Long = -1
    var QualityAuto: Long = -1
    var RemixGenericButtonSize: Long = -1
    var Text1: Long = -1
    var ToolTipContentView: Long = -1
    var TosFooter: Long = -1

    override fun execute(context: ResourceContext) {

        fun find(resourceType: ResourceType, resourceName: String) = ResourceMappingPatch
            .resourceMappings
            .find { it.type == resourceType.value && it.name == resourceName }?.id
            ?: -1

        AccountSwitcherAccessibility = find(STRING, "account_switcher_accessibility_label")
        ButtonContainer = find(ID, "button_container")
        ButtonIconPaddingMedium = find(DIMEN, "button_icon_padding_medium")
        ChipCloud = find(LAYOUT, "chip_cloud")
        ColorGrey = find(COLOR, "ytm_color_grey_12")
        DesignBottomSheetDialog = find(LAYOUT, "design_bottom_sheet_dialog")
        DialogSolid = find(STYLE, "Theme.YouTubeMusic.Dialog.Solid")
        FloatingLayout = find(ID, "floating_layout")
        HistoryMenuItem = find(ID, "history_menu_item")
        InlineTimeBarAdBreakMarkerColor = find(COLOR, "inline_time_bar_ad_break_marker_color")
        IsTablet = find(BOOL, "is_tablet")
        LikeDislikeContainer = find(ID, "like_dislike_container")
        MenuEntry = find(LAYOUT, "menu_entry")
        MiniPlayerMdxPlaying = find(STRING, "mini_player_mdx_playing")
        MusicMenuLikeButtons = find(LAYOUT, "music_menu_like_buttons")
        MusicNotifierShelf = find(LAYOUT, "music_notifier_shelf")
        MusicTastebuilderShelf = find(LAYOUT, "music_tastebuilder_shelf")
        NamesInactiveAccountThumbnailSize = find(DIMEN, "names_inactive_account_thumbnail_size")
        OfflineSettingsMenuItem = find(ID, "offline_settings_menu_item")
        PlayerCastMediaRouteButton = find(LAYOUT, "player_cast_media_route_button")
        PlayerOverlayChip = find(ID, "player_overlay_chip")
        PrivacyTosFooter = find(ID, "privacy_tos_footer")
        QualityAuto = find(STRING, "quality_auto")
        RemixGenericButtonSize = find(DIMEN, "remix_generic_button_size")
        Text1 = find(ID, "text1")
        ToolTipContentView = find(LAYOUT, "tooltip_content_view")
        TosFooter = find(ID, "tos_footer")

    }
}