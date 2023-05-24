package app.revanced.patches.youtube.layout.hide.loadmorebutton.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.mapping.misc.patch.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.loadmorebutton.annotations.HideLoadMoreButtonCompatibility
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Name("hide-load-more-button-resource-patch")
@DependsOn([SettingsPatch::class, ResourceMappingPatch::class])
@HideLoadMoreButtonCompatibility
class HideLoadMoreButtonResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_load_more_button",
                StringResource("revanced_hide_load_more_button_title", "Hide Load More button"),
                StringResource("revanced_hide_load_more_button_summary_on", "Load More button is hidden"),
                StringResource("revanced_hide_load_more_button_summary_off", "Load More button is shown")
            )
        )

        expandButtonDownId = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "expand_button_down"
        }.id

        return PatchResultSuccess()
    }

    internal companion object {
        var expandButtonDownId: Long = -1
    }
}