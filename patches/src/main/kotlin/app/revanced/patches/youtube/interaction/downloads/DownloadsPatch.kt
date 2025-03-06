package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.InputType
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.settings.preference.TextPreference
import app.revanced.patches.youtube.misc.playercontrols.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityFingerprint
import app.revanced.patches.youtube.video.information.videoInformationPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

private val downloadsResourcePatch = resourcePatch {
    dependsOn(
        playerControlsResourcePatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "interaction.downloads.downloadsResourcePatch")

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

        copyResources(
            "downloads",
            ResourceGroup("drawable", "revanced_yt_download_button.xml"),
        )

        addBottomControl("downloads")
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/DownloadsPatch;"

internal const val BUTTON_DESCRIPTOR = "Lapp/revanced/extension/youtube/videoplayer/ExternalDownloadButton;"

@Suppress("unused")
val downloadsPatch = bytecodePatch(
    name = "Downloads",
    description = "Adds support to download videos with an external downloader app " +
        "using the in-app download button or a video player action button.",
) {
    dependsOn(
        downloadsResourcePatch,
        playerControlsPatch,
        videoInformationPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        initializeBottomControl(BUTTON_DESCRIPTOR)
        injectVisibilityCheckCall(BUTTON_DESCRIPTOR)

        // Main activity is used to launch downloader intent.
        mainActivityFingerprint.method.apply {
            addInstruction(
                implementation!!.instructions.lastIndex,
                "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->activityCreated(Landroid/app/Activity;)V",
            )
        }

        offlineVideoEndpointFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static/range {p3 .. p3}, $EXTENSION_CLASS_DESCRIPTOR->inAppDownloadButtonOnClick(Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :show_native_downloader
                    return-void
                    :show_native_downloader
                    nop
                """,
            )
        }
    }
}
