package app.revanced.patches.youtube.interaction.downloads.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.interaction.downloads.annotation.ExternalDownloadsCompatibility
import app.revanced.patches.youtube.interaction.downloads.resource.patch.ExternalDownloadsResourcePatch
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.video.information.patch.VideoInformationPatch

@Patch
@Name("External downloads")
@DependsOn([ExternalDownloadsResourcePatch::class, PlayerControlsBytecodePatch::class, VideoInformationPatch::class])
@Description("Adds support to download and save YouTube videos using an external app.")
@ExternalDownloadsCompatibility
class ExternalDownloadsBytecodePatch : BytecodePatch() {
    private companion object {
        const val BUTTON_DESCRIPTOR = "Lapp/revanced/integrations/videoplayer/ExternalDownloadButton;"
    }

    override fun execute(context: BytecodeContext) {
        /*
        initialize the control
         */

        PlayerControlsBytecodePatch.initializeControl(
            "$BUTTON_DESCRIPTOR->initializeButton(Landroid/view/View;)V")

        /*
         add code to change the visibility of the control
         */

        PlayerControlsBytecodePatch.injectVisibilityCheckCall(
            "$BUTTON_DESCRIPTOR->changeVisibility(Z)V")
    }
}