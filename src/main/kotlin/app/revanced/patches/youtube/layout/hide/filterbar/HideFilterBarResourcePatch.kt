package app.revanced.patches.youtube.layout.hide.filterbar

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var filterBarHeightId = -1L
    private set
internal var relatedChipCloudMarginId = -1L
    private set
internal var barContainerHeightId = -1L
    private set

val hideFilterBarResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.filterbar.HideFilterBarResourcePatch")

        PreferenceScreen.FEED.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_hide_filter_bar_screen",
                preferences = setOf(
                    SwitchPreference("revanced_hide_filter_bar_feed_in_feed"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_search"),
                    SwitchPreference("revanced_hide_filter_bar_feed_in_related_videos"),
                ),
            ),
        )

        relatedChipCloudMarginId = resourceMappings["layout", "related_chip_cloud_reduced_margins"]
        filterBarHeightId = resourceMappings["dimen", "filter_bar_height"]
        barContainerHeightId = resourceMappings["dimen", "bar_container_height"]
    }
}
