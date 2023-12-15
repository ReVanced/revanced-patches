package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch

@Patch(
    dependencies = [SettingsPatch::class, ResourceMappingPatch::class]
)
object RestoreOldVideoQualityMenuResourcePatch : ResourcePatch() {
    internal var videoQualityBottomSheetListFragmentTitle = -1L

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("RestoreOldVideoQualityMenu")
        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference(
                "revanced_restore_old_video_quality_menu",
                "revanced_restore_old_video_quality_menu_title",
                "revanced_restore_old_video_quality_menu_summary_on",
                "revanced_restore_old_video_quality_menu_summary_off"
            )
        )

        fun findResource(name: String) =
            ResourceMappingPatch.resourceMappings.find { it.name == name }?.id
                ?: throw PatchException("Could not find resource")

        // Used for the old type of the video quality menu.
        videoQualityBottomSheetListFragmentTitle =
            findResource("video_quality_bottom_sheet_list_fragment_title")
    }
}
