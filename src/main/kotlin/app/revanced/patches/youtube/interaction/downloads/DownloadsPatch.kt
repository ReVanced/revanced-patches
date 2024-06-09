package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.interaction.downloads.fingerprints.offlineVideoEndpointFingerprint
import app.revanced.patches.youtube.misc.playercontrols.initializeControl
import app.revanced.patches.youtube.misc.playercontrols.injectVisibilityCheckCall
import app.revanced.patches.youtube.misc.playercontrols.playerControlsPatch
import app.revanced.patches.youtube.shared.fingerprints.mainActivityFingerprint
import app.revanced.patches.youtube.video.information.videoInformationPatch

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
        ),
    )

    val offlineVideoEndpointResult by offlineVideoEndpointFingerprint
    val mainActivityResult by mainActivityFingerprint

    val integrationsClassDescriptor = "Lapp/revanced/integrations/youtube/patches/DownloadsPatch;"
    val buttonDescriptor = "Lapp/revanced/integrations/youtube/videoplayer/ExternalDownloadButton;"

    execute {
        initializeControl("$buttonDescriptor->initializeButton(Landroid/view/View;)V")
        injectVisibilityCheckCall("$buttonDescriptor->changeVisibility(Z)V")

        // Main activity is used to launch downloader intent.
        mainActivityResult.mutableMethod.apply {
            addInstruction(
                implementation!!.instructions.lastIndex,
                "invoke-static { p0 }, $integrationsClassDescriptor->activityCreated(Landroid/app/Activity;)V",
            )
        }

        offlineVideoEndpointResult.mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static/range {p3 .. p3}, $integrationsClassDescriptor->inAppDownloadButtonOnClick(Ljava/lang/String;)Z
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
