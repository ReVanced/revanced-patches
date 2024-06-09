package app.revanced.patches.youtube.layout.hide.endscreencards

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

internal var layoutCircle = -1L
    private set
internal var layoutIcon = -1L
    private set
internal var layoutVideo = -1L
    private set

val hideEndscreenCardsResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.endscreencards.HideEndscreenCardsResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_endscreen_cards"),
        )

        fun idOf(name: String) = resourceMappings["layout", "endscreen_element_layout_$name"]

        layoutCircle = idOf("circle")
        layoutIcon = idOf("icon")
        layoutVideo = idOf("video")
    }
}
