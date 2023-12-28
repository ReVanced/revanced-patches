package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources
import app.revanced.util.mergeStrings

@Patch(
    dependencies = [
        BottomControlsResourcePatch::class,
        SettingsPatch::class
    ]
)
internal object ExternalDownloadsResourcePatch : ResourcePatch() {

    override fun execute(context: ResourceContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_external_downloader_preference_screen",
                StringResource("revanced_external_downloader_preference_screen_title", "External download settings"),
                listOf(
                    SwitchPreference(
                        "revanced_external_downloader",
                        StringResource("revanced_external_downloader_title", "Show external download button"),
                        StringResource("revanced_external_downloader_summary_on", "Download button shown in player"),
                        StringResource(
                            "revanced_external_downloader_summary_off",
                            "Download button not shown in player"
                        )
                    ),
                    TextPreference(
                        "revanced_external_downloader_name",
                        StringResource("revanced_external_downloader_name_title", "Downloader package name"),
                        StringResource(
                            "revanced_external_downloader_name_summary",
                            "Package name of your installed external downloader app, such as NewPipe or Seal"
                        ),
                        InputType.TEXT
                    )
                ),
                StringResource(
                    "revanced_external_downloader_preference_screen_summary",
                    "Settings for using an external downloader"
                )
            )
        )

        context.mergeStrings("downloads/host/values/strings.xml")

        context.copyResources(
            "downloads",
            ResourceGroup("drawable", "revanced_yt_download_button.xml")
        )

        BottomControlsResourcePatch.addControls("downloads")
    }
}
