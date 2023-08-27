package app.revanced.patches.youtube.interaction.downloads.resource.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patches.shared.settings.preference.impl.*
import app.revanced.patches.youtube.misc.playercontrols.resource.patch.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources
import app.revanced.util.resources.ResourceUtils.mergeStrings

@DependsOn([BottomControlsResourcePatch::class, SettingsPatch::class])
class ExternalDownloadsResourcePatch : ResourcePatch {

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
                        StringResource("revanced_external_downloader_summary_off", "Download button not shown in player")
                    ),
                    TextPreference(
                        "revanced_external_downloader_name",
                        StringResource("revanced_external_downloader_name_title", "Downloader package name"),
                        StringResource("revanced_external_downloader_name_summary", "Package name of your installed external downloader app, such as NewPipe or Seal"),
                        InputType.TEXT
                    )
                ),
                StringResource("revanced_external_downloader_preference_screen_summary", "Settings for using an external downloader")
            )
        )


        // Copy strings
        context.mergeStrings("downloads/host/values/strings.xml")

        // Copy resources
        context.copyResources("downloads", ResourceUtils.ResourceGroup("drawable", "revanced_yt_download_button.xml"))

        // Add download button node
        BottomControlsResourcePatch.addControls("downloads/host/layout/${BottomControlsResourcePatch.TARGET_RESOURCE_NAME}")
    }
}
