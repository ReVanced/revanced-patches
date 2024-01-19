package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.mapping.misc.ResourceMappingPatch
import app.revanced.patches.shared.settings.preference.impl.IntentPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resource.StringResource

@Patch(
    dependencies = [
        SettingsPatch::class,
        AddResourcesPatch::class
    ]
)
internal object ReturnYouTubeDislikeResourcePatch : ResourcePatch() {
    internal var oldUIDislikeId: Long = -1

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            IntentPreference(
                StringResource("revanced_ryd_settings_title", "Return YouTube Dislike"),
                StringResource("revanced_ryd_settings_summary", "Settings for Return YouTube Dislike"),
                SettingsPatch.newIntent("ryd_settings")
            )
        )

        AddResourcesPatch(this::class)

        oldUIDislikeId = ResourceMappingPatch.resourceMappings.single {
            it.type == "id" && it.name == "dislike_button"
        }.id
    }
}