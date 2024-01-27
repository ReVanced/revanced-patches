package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

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
        AddResourcesPatch::class
    ],
)
internal object DisableSuggestedVideoEndScreenResourcePatch : ResourcePatch() {
    internal var sizeAdjustableLiteAutoNavOverlay: Long = -1

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference("revanced_disable_suggested_video_end_screen")
        )

        sizeAdjustableLiteAutoNavOverlay = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "size_adjustable_lite_autonav_overlay"
        }.id
    }
}