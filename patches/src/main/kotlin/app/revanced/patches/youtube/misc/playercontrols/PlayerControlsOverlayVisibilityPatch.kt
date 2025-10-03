package app.revanced.patches.youtube.misc.playercontrols

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_PLAYER_CONTROLS_VISIBILITY_HOOK_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/PlayerControlsVisibilityHookPatch;"

val playerControlsOverlayVisibilityPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch)

    execute {
        playerControlsVisibilityEntityModelFingerprint.let {
            it.method.apply {
                val startIndex = it.instructionMatches.first().index
                val iGetReference = getInstruction<ReferenceInstruction>(startIndex).reference
                val staticReference = getInstruction<ReferenceInstruction>(startIndex + 1).reference

                it.classDef.methods.find { method -> method.name == "<init>" }?.apply {
                    val targetIndex = indexOfFirstInstructionOrThrow(Opcode.IPUT_OBJECT)
                    val targetRegister = getInstruction<TwoRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1,
                        """
                            iget v$targetRegister, v$targetRegister, $iGetReference
                            invoke-static { v$targetRegister }, $staticReference
                            move-result-object v$targetRegister
                            invoke-static { v$targetRegister }, $EXTENSION_PLAYER_CONTROLS_VISIBILITY_HOOK_CLASS_DESCRIPTOR->setPlayerControlsVisibility(Ljava/lang/Enum;)V
                        """
                    )
                }
            }
        }
    }
}
