package app.revanced.patches.youtube.layout.hide.filterbar.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
class HideFilterBarResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            PreferenceScreen(
                "revanced_hide_filter_bar_preference",
                StringResource(
                    "revanced_hide_filter_bar_preference_title",
                    "Hide filter bar"
                ),
                listOf(
                    SwitchPreference(
                        "revanced_hide_filter_bar_feed_in_feed",
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_feed_title",
                            "Hide in feed"
                        ),
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_feed_summary_on",
                            "Hidden in feed"
                        ),
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_feed_summary_off",
                            "Shown in feed"
                        )
                    ),
                    SwitchPreference(
                        "revanced_hide_filter_bar_feed_in_search",
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_search_title",
                            "Hide in search"
                        ),
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_search_summary_on",
                            "Hidden in search"
                        ),
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_search_summary_off",
                            "Shown in search"
                        )
                    ),
                    SwitchPreference(
                        "revanced_hide_filter_bar_feed_in_related_videos",
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_related_videos_title",
                            "Hide in related videos"
                        ),
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_related_videos_summary_on",
                            "Hidden in related videos"
                        ),
                        StringResource(
                            "revanced_hide_filter_bar_feed_in_related_videos_summary_off",
                            "Shown in related videos"
                        )
                    ),
                ),
                StringResource(
                    "revanced_hide_filter_bar_preference_summary",
                    "Manage the visibility of the filter bar in the feed, search and related videos"
                )
            )
        )

        relatedChipCloudMarginId = "related_chip_cloud_reduced_margins".layoutResourceId("layout")
        filterBarHeightId = "filter_bar_height".layoutResourceId()
        barContainerHeightId = "bar_container_height".layoutResourceId()
    }

    internal companion object {
        var filterBarHeightId = -1L
        var relatedChipCloudMarginId = -1L
        var barContainerHeightId = -1L

        private fun String.layoutResourceId(type: String = "dimen") =
            ResourceMappingPatch.resourceMappings.single { it.type == type && it.name == this }.id
    }
}