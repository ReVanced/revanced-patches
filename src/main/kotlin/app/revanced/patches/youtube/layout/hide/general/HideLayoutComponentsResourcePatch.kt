package app.revanced.patches.youtube.layout.hide.general

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.youtube.layout.hide.crowdfundingbox.CrowdfundingBoxResourcePatch
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
    }
}
