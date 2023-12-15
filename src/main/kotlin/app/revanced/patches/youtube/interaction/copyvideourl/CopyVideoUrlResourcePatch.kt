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
                "revanced_copy_video_url_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_copy_video_url",
                        "revanced_copy_video_url_title",
                        "revanced_copy_video_url_summary_on",
                        "revanced_copy_video_url_summary_off"
                    ),
                    SwitchPreference(
                        "revanced_copy_video_url_timestamp",
                        "revanced_copy_video_url_timestamp_title",
                        "revanced_copy_video_url_timestamp_summary_on",
                        "revanced_copy_video_url_timestamp_summary_off"
                    )
                ),
                "revanced_copy_video_url_preference_screen_summary"
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

        BottomControlsResourcePatch.addControls("youtube/copyvideourl/host/layout/${BottomControlsResourcePatch.TARGET_RESOURCE_NAME}")
    }
}