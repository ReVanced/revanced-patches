package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.mapping.ResourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.settings.SettingsResourcePatch

@Patch(
    dependencies = [
        SettingsPatch::class,
        AddResourcesPatch::class,
    ],
)
internal object ReturnYouTubeDislikeResourcePatch : ResourcePatch() {
    internal var oldUIDislikeId: Long = -1

    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsResourcePatch += IntentPreference(
            key = "revanced_settings_screen_09",
            titleKey = "revanced_ryd_settings_title",
            summaryKey = null,
            intent = SettingsPatch.newIntent("revanced_ryd_settings_intent"),
        )

        oldUIDislikeId = ResourceMappingPatch[
            "id",
            "dislike_button",
        ]
    }
}
