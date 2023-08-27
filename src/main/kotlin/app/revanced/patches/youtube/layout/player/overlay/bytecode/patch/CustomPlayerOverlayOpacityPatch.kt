package app.revanced.patches.youtube.layout.player.overlay.bytecode.patch

import app.revanced.extensions.indexOfFirstConstantInstructionValue
import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.player.overlay.annotations.PlayerOverlayPatchCompatibility
import app.revanced.patches.youtube.layout.player.overlay.bytecode.fingerprints.CreatePlayerOverviewFingerprint
import app.revanced.patches.youtube.layout.player.overlay.resource.patch.CustomPlayerOverlayOpacityResourcePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("Custom player overlay opacity")
@Description("Change the opacity of the player background, when player controls are visible.")
@DependsOn([CustomPlayerOverlayOpacityResourcePatch::class])
@PlayerOverlayPatchCompatibility
class CustomPlayerOverlayOpacityPatch : BytecodePatch(listOf(CreatePlayerOverviewFingerprint)) {
    override fun execute(context: BytecodeContext) {
        CreatePlayerOverviewFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val viewRegisterIndex =
                    indexOfFirstConstantInstructionValue(CustomPlayerOverlayOpacityResourcePatch.scrimOverlayId) + 3
                val viewRegister =
                    getInstruction<OneRegisterInstruction>(viewRegisterIndex).registerA

                val insertIndex = viewRegisterIndex + 1
                addInstruction(
                    insertIndex,
                    "invoke-static { v$viewRegister }, " +
                            "$INTEGRATIONS_CLASS_DESCRIPTOR->changeOpacity(Landroid/widget/ImageView;)V"
                )
            }
        } ?: throw CreatePlayerOverviewFingerprint.exception
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/CustomPlayerOverlayOpacityPatch;"
    }
}
