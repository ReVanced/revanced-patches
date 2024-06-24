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
                val conditionalJumpIndex = it.scanResult.patternScanResult!!.endIndex

                val conditionalJumpLabel = getInstruction<BuilderInstruction21t>(conditionalJumpIndex).target

                // Replace this conditional jump by a goto
                replaceInstruction(
                    conditionalJumpIndex,
                    BuilderInstruction10t(
                        Opcode.GOTO,
                        conditionalJumpLabel,
                    ),
                )
            }
        } ?: throw ProfileAdInjectorFingerprint.exception
    }
}
