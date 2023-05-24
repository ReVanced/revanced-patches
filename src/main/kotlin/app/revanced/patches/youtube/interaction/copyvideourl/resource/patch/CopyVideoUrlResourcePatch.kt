package app.revanced.patches.youtube.interaction.copyvideourl.resource.patch

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.resource.patch.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.mergeStrings

@Name("copy-video-url-resource")
@DependsOn([BottomControlsResourcePatch::class, SettingsPatch::class])
@Version("0.0.1")
class CopyVideoUrlResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_copy_video_url_preference_screen",
                StringResource("revanced_copy_video_url_preference_screen_title", "Copy video URL settings"),
                listOf(
                    SwitchPreference(
                        "revanced_copy_video_url",
                        StringResource("revanced_copy_video_url_title", "Show copy video URL button"),
                        StringResource("revanced_copy_video_url_summary_on", "Button is shown. Tap to copy video URL. Tap and hold to copy video URL with timestamp"),
                        StringResource("revanced_copy_video_url_summary_off", "Button is not shown")
                    ),
                    SwitchPreference(
                        "revanced_copy_video_url_timestamp",
                        StringResource("revanced_copy_video_url_timestamp_title", "Show copy timestamp URL button"),
                        StringResource("revanced_copy_video_url_timestamp_summary_on", "Button is shown.  Tap to copy video URL with timestamp. Tap and hold to copy video without timestamp"),
                        StringResource("revanced_copy_video_url_timestamp_summary_off", "Button is not shown")
                    )
                ),
                StringResource("revanced_copy_video_url_preference_screen_summary", "Settings related to copy URL buttons in video player")
            )
        )

        context.copyResources("copyvideourl", ResourceUtils.ResourceGroup(
            resourceDirectoryName = "drawable",
            "revanced_yt_copy.xml",
            "revanced_yt_copy_timestamp.xml"
        ))

        // merge strings
        context.mergeStrings("copyvideourl/host/values/strings.xml")

        BottomControlsResourcePatch.addControls("copyvideourl/host/layout/${BottomControlsResourcePatch.TARGET_RESOURCE_NAME}")

        return PatchResultSuccess()
    }
}