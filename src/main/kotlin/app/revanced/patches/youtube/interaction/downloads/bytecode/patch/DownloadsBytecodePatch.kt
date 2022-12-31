package app.revanced.patches.youtube.interaction.downloads.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.interaction.downloads.annotation.DownloadsCompatibility
import app.revanced.patches.youtube.interaction.downloads.resource.patch.DownloadsResourcePatch
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.misc.video.information.patch.VideoInformationPatch

@Patch
@Name("downloads")
@DependsOn([DownloadsResourcePatch::class, PlayerControlsBytecodePatch::class, VideoInformationPatch::class])
@Description("Enables downloading music and videos from YouTube.")
@DownloadsCompatibility
@Version("0.0.1")
class DownloadsBytecodePatch : BytecodePatch() {
    override fun execute(context: BytecodeContext): PatchResult {
        val integrationsPackage = "app/revanced/integrations"
        val classDescriptor = "L$integrationsPackage/videoplayer/DownloadButton;"

        /*
        initialize the control
         */

        val initializeDownloadsDescriptor = "$classDescriptor->initializeButton(Ljava/lang/Object;)V"
        PlayerControlsBytecodePatch.initializeControl(initializeDownloadsDescriptor)

        /*
         add code to change the visibility of the control
         */

        val changeVisibilityDescriptor = "$classDescriptor->changeVisibility(Z)V"
        PlayerControlsBytecodePatch.injectVisibilityCheckCall(changeVisibilityDescriptor)

        return PatchResultSuccess()
    }
}