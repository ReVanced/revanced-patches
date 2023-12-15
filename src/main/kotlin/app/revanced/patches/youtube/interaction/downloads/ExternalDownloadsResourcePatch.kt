package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.InputType
import app.revanced.patches.shared.settings.preference.impl.PreferenceScreen
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.shared.settings.preference.impl.TextPreference
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    dependencies = [
        BottomControlsResourcePatch::class,
        SettingsPatch::class
    ]
)
internal object ExternalDownloadsResourcePatch : ResourcePatch() {

    override fun execute(context: ResourceContext) {
        StringsPatch.includePatchStrings("ExternalDownloads")
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            PreferenceScreen(
                "revanced_external_downloader_preference_screen",
                "revanced_external_downloader_preference_screen_title",
                listOf(
                    SwitchPreference(
                        "revanced_external_downloader",
                        "revanced_external_downloader_title",
                        "revanced_external_downloader_summary_on",
                        "revanced_external_downloader_summary_off"
                    ),
                    TextPreference(
                        "revanced_external_downloader_name",
                        "revanced_external_downloader_name_title",
                        "revanced_external_downloader_name_summary",
                        InputType.TEXT
                    )
                ),
                "revanced_external_downloader_preference_screen_summary"
            )
        )

        // Copy resources
        context.copyResources(
            "youtube/downloads",
            ResourceGroup("drawable", "revanced_ic_download_button.xml")
        )

        // Add download button node
        BottomControlsResourcePatch.addControls("youtube/downloads/host/layout/${BottomControlsResourcePatch.TARGET_RESOURCE_NAME}")
    }
}
