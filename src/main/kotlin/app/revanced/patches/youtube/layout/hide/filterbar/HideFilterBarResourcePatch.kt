package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(dependencies = [SettingsPatch::class, ResourceMappingPatch::class])
internal object HideFilterBarResourcePatch : ResourcePatch() {
    internal var filterBarHeightId = -1L
    internal var relatedChipCloudMarginId = -1L
    internal var barContainerHeightId = -1L

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("HideFilterBar")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_filter_bar_screen",
                listOf(
                    SwitchPreference("revanced_hide_filter_bar_feed_in_feed"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_search"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_related_videos")
                )
            )
        )

        relatedChipCloudMarginId = "related_chip_cloud_reduced_margins".layoutResourceId("layout")
        filterBarHeightId = "filter_bar_height".layoutResourceId()
        barContainerHeightId = "bar_container_height".layoutResourceId()
    }

    private fun String.layoutResourceId(type: String = "dimen") =
        ResourceMappingPatch.resourceMappings.single { it.type == type && it.name == this }.id
}