package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.interaction.downloads.fingerprints.OfflineVideoEndpointFingerprint
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.shared.fingerprints.MainActivityFingerprint
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.util.resultOrThrow

@Patch(
    name = "Downloads",
    description = "Adds support to download videos with an external downloader app " +
        "using the in-app download button or a video player action button.",
    dependencies = [
        DownloadsResourcePatch::class,
        PlayerControlsBytecodePatch::class,
        VideoInformationPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
)
@Suppress("unused")
object DownloadsPatch : BytecodePatch(
    setOf(
        OfflineVideoEndpointFingerprint,
        MainActivityFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/DownloadsPatch;"
    private const val BUTTON_DESCRIPTOR = "Lapp/revanced/integrations/youtube/videoplayer/ExternalDownloadButton;"

    override fun execute(context: BytecodeContext) {
        PlayerControlsBytecodePatch.initializeBottomControl(BUTTON_DESCRIPTOR)
        PlayerControlsBytecodePatch.injectVisibilityCheckCall(BUTTON_DESCRIPTOR)

        // Main activity is used to launch downloader intent.
        MainActivityFingerprint.resultOrThrow().mutableMethod.apply {
            addInstruction(
                implementation!!.instructions.lastIndex,
                "invoke-static { p0 }, $INTEGRATIONS_CLASS_DESCRIPTOR->activityCreated(Landroid/app/Activity;)V"
            )
        }

        OfflineVideoEndpointFingerprint.resultOrThrow().mutableMethod.apply {
            addInstructionsWithLabels(
                0,
                """
                    invoke-static/range {p3 .. p3}, $INTEGRATIONS_CLASS_DESCRIPTOR->inAppDownloadButtonOnClick(Ljava/lang/String;)Z
                    move-result v0
                    if-eqz v0, :show_native_downloader
                    return-void
                    :show_native_downloader
                    nop
                """
            )
        }
    }
}
