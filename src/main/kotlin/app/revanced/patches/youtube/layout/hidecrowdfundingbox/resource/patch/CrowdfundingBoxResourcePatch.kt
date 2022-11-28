package app.revanced.patches.youtube.layout.hidecrowdfundingbox.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.hidecrowdfundingbox.annotations.CrowdfundingBoxCompatibility
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference

@Name("crowdfunding-box-resource-patch")
@CrowdfundingBoxCompatibility
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@Version("0.0.1")
class CrowdfundingBoxResourcePatch : ResourcePatch {
    companion object {
        internal var crowdfundingBoxId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_crowdfunding_box",
                StringResource("revanced_hide_crowdfunding_box_title", "Hide the crowdfunding box"),
                false,
                StringResource("revanced_hide_crowdfunding_box_summary_on", "Crowdfunding box is hidden"),
                StringResource("revanced_hide_crowdfunding_box_summary_off", "Crowdfunding box is visible")
            )
        )

        crowdfundingBoxId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "donation_companion"
        }.id

        return PatchResultSuccess()
    }
}