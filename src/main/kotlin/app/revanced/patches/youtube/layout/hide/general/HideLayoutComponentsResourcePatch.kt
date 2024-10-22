package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ],
)
internal object HideLayoutComponentsResourcePatch : ResourcePatch() {
    var expandButtonDownId: Long = -1
    var albumCardId: Long = -1
    var crowdfundingBoxId: Long = -1
    var youTubeLogo = -1L

    var filterBarHeightId = -1L
    var relatedChipCloudMarginId = -1L
    var barContainerHeightId = -1L

    var fabButtonId: Long = -1

    override fun execute(context: ResourceContext) {
        expandButtonDownId = ResourceMappingPatch[
            "layout",
            "expand_button_down",
        ]

        albumCardId = ResourceMappingPatch[
            "layout",
            "album_card"
        ]

        crowdfundingBoxId = ResourceMappingPatch[
            "layout",
            "donation_companion",
        ]

        youTubeLogo = ResourceMappingPatch[
            "id",
            "youtube_logo"
        ]

        relatedChipCloudMarginId = ResourceMappingPatch[
            "layout",
            "related_chip_cloud_reduced_margins"
        ]

        filterBarHeightId = ResourceMappingPatch[
            "dimen",
            "filter_bar_height"
        ]

        barContainerHeightId = ResourceMappingPatch[
            "dimen",
            "bar_container_height"
        ]

        fabButtonId = ResourceMappingPatch[
            "id",
            "fab"
        ]
    }
}
