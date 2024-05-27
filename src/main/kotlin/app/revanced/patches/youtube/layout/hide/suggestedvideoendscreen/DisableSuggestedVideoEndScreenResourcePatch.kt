package app.revanced.patches.youtube.layout.hide.suggestedvideoendscreen

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var sizeAdjustableLiteAutoNavOverlay = -1L
    private set

internal val disableSuggestedVideoEndScreenResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.suggestedvideoendscreen.DisableSuggestedVideoEndScreenResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_disable_suggested_video_end_screen"),
        )

        sizeAdjustableLiteAutoNavOverlay = resourceMappings[
            "layout",
            "size_adjustable_lite_autonav_overlay",
        ]
    }
}
