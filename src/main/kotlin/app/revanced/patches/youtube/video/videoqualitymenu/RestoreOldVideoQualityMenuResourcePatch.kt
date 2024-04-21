package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    dependencies = [SettingsPatch::class, ResourceMappingPatch::class, AddResourcesPatch::class],
)
object RestoreOldVideoQualityMenuResourcePatch : ResourcePatch() {
    internal var videoQualityBottomSheetListFragmentTitle = -1L

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_restore_old_video_quality_menu"),
        )

        // Used for the old type of the video quality menu.
        videoQualityBottomSheetListFragmentTitle = ResourceMappingPatch[
            "layout",
            "video_quality_bottom_sheet_list_fragment_title",
        ]
    }
}
