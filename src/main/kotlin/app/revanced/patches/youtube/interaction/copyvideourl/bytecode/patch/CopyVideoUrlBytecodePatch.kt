package app.revanced.patches.youtube.interaction.copyvideourl.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.interaction.copyvideourl.annotation.CopyVideoUrlCompatibility
import app.revanced.patches.youtube.interaction.copyvideourl.resource.patch.CopyVideoUrlResourcePatch
import app.revanced.patches.youtube.misc.playercontrols.bytecode.patch.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.video.information.patch.VideoInformationPatch

@Patch
@Name("Copy video url")
@Description("Adds buttons in player to copy video links.")
@DependsOn([
    CopyVideoUrlResourcePatch::class,
    PlayerControlsBytecodePatch::class,
    VideoInformationPatch::class
])
@CopyVideoUrlCompatibility
class CopyVideoUrlBytecodePatch : BytecodePatch() {
    private companion object {
        const val INTEGRATIONS_PLAYER_PACKAGE = "Lapp/revanced/integrations/videoplayer"
        val BUTTONS_DESCRIPTORS = listOf(
            "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlButton;",
            "$INTEGRATIONS_PLAYER_PACKAGE/CopyVideoUrlTimestampButton;"
        )
    }

    override fun execute(context: BytecodeContext) {

        // Initialize buttons and inject visibility control
        BUTTONS_DESCRIPTORS.forEach { descriptor ->
            PlayerControlsBytecodePatch.initializeControl("$descriptor->initializeButton(Landroid/view/View;)V")
            PlayerControlsBytecodePatch.injectVisibilityCheckCall("$descriptor->changeVisibility(Z)V")
        }
    }
}