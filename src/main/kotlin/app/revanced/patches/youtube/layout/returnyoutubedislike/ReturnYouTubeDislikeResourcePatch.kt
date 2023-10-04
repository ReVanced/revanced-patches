package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.Preference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resources.ResourceUtils.mergeStrings

@Patch(
    dependencies = [SettingsPatch::class]
)
object ReturnYouTubeDislikeResourcePatch : ResourcePatch() {
    internal var oldUIDislikeId: Long = -1

    override fun execute(context: ResourceContext) {
        SettingsPatch.addPreference(
            Preference(
                StringResource("revanced_ryd_settings_title", "Return YouTube Dislike"),
                StringResource("revanced_ryd_settings_summary", "Settings for Return YouTube Dislike"),
                SettingsPatch.createReVancedSettingsIntent("ryd_settings")
            )
        )
        // merge strings
        context.mergeStrings("returnyoutubedislike/host/values/strings.xml")

        oldUIDislikeId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "dislike_button"
        }.id
    }
}