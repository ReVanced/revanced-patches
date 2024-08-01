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
        bottomControlsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute { context ->
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

        context.copyResources(
            "downloads",
            ResourceGroup("drawable", "revanced_yt_download_button.xml"),
        )

        addBottomControls("downloads")
    }
}

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/DownloadsPatch;"

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
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val offlineVideoEndpointMatch by offlineVideoEndpointFingerprint()
    val mainActivityMatch by mainActivityFingerprint()

    execute {
        initializeControl("$BUTTON_DESCRIPTOR->initializeButton(Landroid/view/View;)V")
        injectVisibilityCheckCall("$BUTTON_DESCRIPTOR->changeVisibility(Z)V")

        // Main activity is used to launch downloader intent.
        mainActivityMatch.mutableMethod.apply {
            addInstruction(
                implementation!!.instructions.lastIndex,
                "invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->activityCreated(Landroid/app/Activity;)V",
            )
        }

        offlineVideoEndpointMatch.mutableMethod.apply {
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
