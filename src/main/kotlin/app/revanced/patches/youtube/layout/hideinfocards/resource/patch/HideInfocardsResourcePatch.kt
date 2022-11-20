package app.revanced.patches.youtube.layout.hideinfocards.resource.patch

import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.youtube.layout.hideinfocards.annotations.HideInfocardsCompatibility
import app.revanced.patches.youtube.misc.mapping.patch.ResourceMappingPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference

@HideInfocardsCompatibility
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@Version("0.0.1")
class HideInfocardsResourcePatch : ResourcePatch {
    internal companion object {
        var drawerResourceId: Long = -1
    }

    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_infocards",
                StringResource("revanced_hide_infocards_title", "Hide info-cards"),
                true,
                StringResource("revanced_hide_infocards_summary_on", "Info-cards are hidden"),
                StringResource("revanced_hide_infocards_summary_off", "Info-cards are shown")
            )
        )

        drawerResourceId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "info_cards_drawer_header"
        }.id

        return PatchResultSuccess()
    }
}