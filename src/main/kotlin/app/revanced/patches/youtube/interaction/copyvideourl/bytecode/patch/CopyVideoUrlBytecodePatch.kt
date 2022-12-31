package app.revanced.patches.youtube.interaction.copyvideourl.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.interaction.copyvideourl.annotation.CopyVideoUrlCompatibility
import app.revanced.patches.youtube.interaction.copyvideourl.resource.patch.CopyVideoUrlResourcePatch
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.misc.video.information.patch.VideoInformationPatch

@Patch
@Name("copy-video-url")
@Description("Adds buttons in player to copy video links.")
@DependsOn([
    CopyVideoUrlResourcePatch::class,
    PlayerControlsBytecodePatch::class,
    VideoInformationPatch::class
])
@CopyVideoUrlCompatibility
@Version("0.0.1")
class CopyVideoUrlBytecodePatch : BytecodePatch() {
    private companion object {
        const val INTEGRATIONS_PACKAGE = "Lapp/revanced/integrations"
        const val INTEGRATIONS_PLAYER_PACKAGE = "$INTEGRATIONS_PACKAGE/videoplayer"
        val BUTTONS_DESCRIPTORS = listOf(
            "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlButton;",
            "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlTimestampButton;"
        )
    }

    override fun execute(context: BytecodeContext): PatchResult {

        // Initialize buttons and inject visibility control
        BUTTONS_DESCRIPTORS.forEach { descriptor ->
            val initializeButtonDescriptor = "$descriptor->initializeButton(Ljava/lang/Object;)V"
            val visibilityDescriptor = "$descriptor->changeVisibility(Z)V"
            PlayerControlsBytecodePatch.initializeControl(initializeButtonDescriptor)
            PlayerControlsBytecodePatch.injectVisibilityCheckCall(visibilityDescriptor)
        }

        return PatchResultSuccess()
    }
}