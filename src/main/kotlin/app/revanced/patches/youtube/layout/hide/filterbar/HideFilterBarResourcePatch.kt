package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class, AddResourcesPatch::class])
internal object HideFilterBarResourcePatch : ResourcePatch() {
    internal var filterBarHeightId = -1L
    internal var relatedChipCloudMarginId = -1L
    internal var barContainerHeightId = -1L

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.FEED.addPreferences(
            PreferenceScreen(
                key = "revanced_hide_filter_bar_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_filter_bar_feed_in_feed"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_search"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_related_videos"),
                ),
            ),
        )

        relatedChipCloudMarginId = ResourceMappingPatch["layout", "related_chip_cloud_reduced_margins"]
        filterBarHeightId = ResourceMappingPatch["dimen", "filter_bar_height"]
        barContainerHeightId = ResourceMappingPatch["dimen", "bar_container_height"]
    }
}
