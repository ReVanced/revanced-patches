package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Patch(
    dependencies = [
        BottomControlsResourcePatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
    ],
)
internal object DownloadsResourcePatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreen(
                key = "revanced_external_downloader_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_external_downloader"),
                    SwitchPreference("revanced_external_downloader_action_button"),
                    TextPreference("revanced_external_downloader_name", inputType = InputType.TEXT),
                ),
            ),
        )

        context.copyResources(
            "downloads",
            ResourceGroup("drawable", "revanced_yt_download_button.xml"),
        )

        BottomControlsResourcePatch.addControls("downloads")
    }
}
