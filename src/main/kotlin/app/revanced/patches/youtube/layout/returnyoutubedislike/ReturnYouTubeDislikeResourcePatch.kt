package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch

@Patch(
    dependencies = [SettingsPatch::class]
)
internal object ReturnYouTubeDislikeResourcePatch : ResourcePatch() {
    internal var oldUIDislikeId: Long = -1

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("ReturnYouTubeDislike")
        SettingsPatch.addPreference(
            Preference(
                "revanced_ryd_settings_title",
                "revanced_ryd_settings_summary",
                SettingsPatch.createReVancedSettingsIntent("ryd_settings_intent")
            )
        )

        oldUIDislikeId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "dislike_button"
        }.id
    }
}