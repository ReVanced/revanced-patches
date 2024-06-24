package app.revanced.patches.instagram.patches.ads.profile

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.instagram.patches.ads.profile.fingerprints.ProfileAdInjectorFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t

@Patch(
    name = "Hide profile ads",
    compatiblePackages = [CompatiblePackage("com.instagram.android")],
)
@Suppress("unused")
object HideProfileAdsPatch : BytecodePatch(
    setOf(
        ProfileAdInjectorFingerprint,
    ),
) {
    override fun execute(context: BytecodeContext) {
        ProfileAdInjectorFingerprint.result?.let {
            it.mutableMethod.apply {
                val firstInstructionIndex = it.scanResult.patternScanResult!!.startIndex
                val conditionInstructionIndex = it.scanResult.patternScanResult!!.endIndex

                val conditionInstructionLabel = getInstruction<BuilderInstruction21t>(conditionInstructionIndex).target

                // Replace the first instruction of the method with a goto to the label from the condition instruction
                replaceInstruction(
                    firstInstructionIndex,
                    BuilderInstruction10t(
                        Opcode.GOTO,
                        conditionInstructionLabel,
                    ),
                )
            }
        } ?: throw ProfileAdInjectorFingerprint.exception
    }
}
