package app.revanced.patches.youtube.layout.hide.endscreencards

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
internal object HideEndscreenCardsResourcePatch : ResourcePatch() {
    internal var layoutCircle: Long = -1
    internal var layoutIcon: Long = -1
    internal var layoutVideo: Long = -1

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(SwitchPreference("revanced_hide_endscreen_cards"))

        fun findEndscreenResourceId(name: String) = ResourceMappingPatch.resourceMappings.single {
            it.type == "layout" && it.name == "endscreen_element_layout_$name"
        }.id

        layoutCircle = findEndscreenResourceId("circle")
        layoutIcon = findEndscreenResourceId("icon")
        layoutVideo = findEndscreenResourceId("video")
    }
}