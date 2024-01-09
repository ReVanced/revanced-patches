package app.revanced.patches.youtube.interaction.copyvideourl

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    dependencies = [
        SettingsPatch::class,
        BottomControlsResourcePatch::class
    ]
)
internal object CopyVideoUrlResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("CopyVideoUrl")
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_copy_video_url_preference_screen",
                listOf(
                    SwitchPreference("revanced_copy_video_url"),
                    SwitchPreference("revanced_copy_video_url_timestamp")
                )
            )
        )

        context.copyResources(
            "youtube/copyvideourl",
            ResourceGroup(
                resourceDirectoryName = "drawable",
                "revanced_ic_copy_video_url.xml",
                "revanced_ic_copy_video_timestamp.xml"
            )
        )

        BottomControlsResourcePatch.addControls("youtube/copyvideourl")
    }
}