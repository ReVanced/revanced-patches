package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.playercontrols.BottomControlsResourcePatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

@Suppress("unused")
val downloadsResourcePatch = resourcePatch {
    dependsOn(
        BottomControlsResourcePatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute { context ->
        addResources("youtube", "interaction.downloads.DownloadsResourcePatch")

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
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
