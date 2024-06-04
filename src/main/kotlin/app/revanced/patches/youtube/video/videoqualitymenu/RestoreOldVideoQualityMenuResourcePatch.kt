package app.revanced.patches.youtube.video.videoqualitymenu

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var videoQualityBottomSheetListFragmentTitle = -1L
    private set
internal var videoQualityQuickMenuAdvancedMenuDescription = -1L
    private set

internal val restoreOldVideoQualityMenuResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "video.videoqualitymenu.RestoreOldVideoQualityMenuResourcePatch")

        PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_restore_old_video_quality_menu"),
        )

        // Used for the old type of the video quality menu.
        videoQualityBottomSheetListFragmentTitle = resourceMappings[
            "layout",
            "video_quality_bottom_sheet_list_fragment_title",
        ]

        videoQualityQuickMenuAdvancedMenuDescription = resourceMappings[
            "string",
            "video_quality_quick_menu_advanced_menu_description",
        ]
    }
}
