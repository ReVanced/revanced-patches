package app.revanced.patches.youtube.interaction.copyvideourl.resource.patch

import app.revanced.patcher.annotation.Description
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
import app.revanced.patches.youtube.interaction.copyvideourl.annotation.CopyVideoUrlCompatibility
import app.revanced.patches.youtube.misc.manifest.patch.FixLocaleConfigErrorPatch
import app.revanced.patches.youtube.misc.playercontrols.resource.patch.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources

@Name("copy-video-url-resource")
@Description("Makes necessary changes to resources for copy video link buttons.")
@DependsOn([BottomControlsResourcePatch::class, FixLocaleConfigErrorPatch::class, SettingsPatch::class])
@CopyVideoUrlCompatibility
@Version("0.0.1")
class CopyVideoUrlResourcePatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_copy_video_url",
                StringResource("revanced_copy_video_url_title", "Copy video URL settings"),
                listOf(
                    SwitchPreference(
                        "revanced_copy_video_url_enabled",
                        StringResource("revanced_copy_video_url_enabled_title", "Show copy video URL button"),
                        true,
                        StringResource("revanced_copy_video_url_enabled_summary_on", "Button is shown, click to copy video URL without timestamp"),
                        StringResource("revanced_copy_video_url_enabled_summary_off", "Button is not shown")
                    ),
                    SwitchPreference(
                        "revanced_copy_video_url_timestamp_enabled",
                        StringResource("revanced_copy_video_url_timestamp_enabled_title", "Show copy timestamp URL button"),
                        true,
                        StringResource("revanced_copy_video_url_timestamp_enabled_summary_on", "Button is shown, click to copy video URL with timestamp"),
                        StringResource("revanced_copy_video_url_timestamp_enabled_summary_off", "Button is not shown")
                    )
                ),
                StringResource("revanced_copy_video_url_summary", "Settings related to copy URL buttons in video player")
            )
        )

        context.copyResources("copyvideourl", ResourceUtils.ResourceGroup(
            resourceDirectoryName = "drawable",
            "revanced_yt_copy.xml",
            "revanced_yt_copy_timestamp.xml"
        ))

        BottomControlsResourcePatch.addControls("copyvideourl/host/layout/${BottomControlsResourcePatch.TARGET_RESOURCE_NAME}")

        return PatchResultSuccess()
    }
}