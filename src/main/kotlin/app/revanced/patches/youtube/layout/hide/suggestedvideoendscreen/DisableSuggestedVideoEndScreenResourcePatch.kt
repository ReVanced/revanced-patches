package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        ResourceMappingPatch::class
    ],
)
object DisableSuggestedVideoEndScreenResourcePatch : ResourcePatch() {
    internal var sizeAdjustableLiteAutoNavOverlay: Long = -1

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_disable_suggested_video_end_screen",
                StringResource(
                    "revanced_disable_suggested_video_end_screen_title",
                    "Disable suggested video end screen"
                ),
                StringResource(
                    "revanced_disable_suggested_video_end_screen_summary_on",
                    "Suggested videos will be disabled"
                ),
                StringResource(
                    "revanced_disable_suggested_video_end_screen_summary_off",
                    "Suggested videos will be shown"
                ),
            )
        )

        sizeAdjustableLiteAutoNavOverlay = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "size_adjustable_lite_autonav_overlay"
        }.id
    }
}