package app.revanced.patches.youtube.interaction.downloads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.interaction.downloads.fingerprints.DownloadButtonActionFingerprint
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import app.revanced.util.exception

@Patch(
    name = "Downloads",
    description = "Adds support to download videos with an external downloader app" +
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
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39",
                "19.03.35",
                "19.03.36",
                "19.04.37",
            ],
        ),
    ],
)
@Suppress("unused")
object DownloadsPatch : BytecodePatch(
    setOf(
        DownloadButtonActionFingerprint,
    ),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/DownloadsPatch;"
    private const val BUTTON_DESCRIPTOR = "Lapp/revanced/integrations/youtube/videoplayer/ExternalDownloadButton;"

    override fun execute(context: BytecodeContext) {
        PlayerControlsBytecodePatch.initializeControl("$BUTTON_DESCRIPTOR->initializeButton(Landroid/view/View;)V")
        PlayerControlsBytecodePatch.injectVisibilityCheckCall("$BUTTON_DESCRIPTOR->changeVisibility(Z)V")

        DownloadButtonActionFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    2,
                    """
                            invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->inAppDownloadButtonOnClick()Z
                            move-result v0
                            if-eqz v0, :show_dialog
                            return-void
                        """,
                    ExternalLabel("show_dialog", getInstruction(2)),
                )
            }
        } ?: throw DownloadButtonActionFingerprint.exception
    }
}
