package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.interaction.downloads.fingerprints.offlineVideoEndpointFingerprint
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.shared.fingerprints.mainActivityFingerprint
import app.revanced.patches.youtube.video.information.VideoInformationPatch

@Suppress("unused")
val downloadsPatch = bytecodePatch(
    name = "Downloads",
    description = "Adds support to download videos with an external downloader app " +
        "using the in-app download button or a video player action button.",
) {
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

    dependsOn(
        downloadsResourcePatch,
        PlayerControlsBytecodePatch,
        VideoInformationPatch,
    )

    val offlineVideoEndpointResult by offlineVideoEndpointFingerprint
    val mainActivityResult by mainActivityFingerprint

    val integrationsClassDescriptor = "Lapp/revanced/integrations/youtube/patches/DownloadsPatch;"
    val buttonDescriptor = "Lapp/revanced/integrations/youtube/videoplayer/ExternalDownloadButton;"

    execute {
        PlayerControlsBytecodePatch.initializeControl("$buttonDescriptor->initializeButton(Landroid/view/View;)V")
        PlayerControlsBytecodePatch.injectVisibilityCheckCall("$buttonDescriptor->changeVisibility(Z)V")

//        mainActivityResult.mutableMethod.implementation?.instructions.lastIndex = ""

        offlineVideoEndpointResult.mutableMethod.addInstructionsWithLabels(
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
