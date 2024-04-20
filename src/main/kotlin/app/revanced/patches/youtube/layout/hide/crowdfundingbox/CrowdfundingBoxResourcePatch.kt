package app.revanced.patches.youtube.layout.hide.crowdfundingbox

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class,
        AddResourcesPatch::class,
    ],
)
internal object CrowdfundingBoxResourcePatch : ResourcePatch() {
    internal var crowdfundingBoxId: Long = -1

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.FEED.addPreferences(
            SwitchPreference("revanced_hide_crowdfunding_box"),
        )

        crowdfundingBoxId = ResourceMappingPatch[
            "layout",
            "donation_companion",
        ]
    }
}
