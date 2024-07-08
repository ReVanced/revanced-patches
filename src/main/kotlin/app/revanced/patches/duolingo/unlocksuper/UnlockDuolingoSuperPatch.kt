package app.revanced.patches.duolingo.unlocksuper

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.duolingo.unlocksuper.fingerprints.IsUserSuperMethodFingerprint
import app.revanced.patches.duolingo.unlocksuper.fingerprints.UserSerializationMethodFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.Reference

@Patch(
    name = "Unlock Duolingo Super",
    compatiblePackages = [CompatiblePackage("com.duolingo")]
)
@Suppress("unused")
object UnlockDuolingoSuperPatch : BytecodePatch(
    setOf(
        UserSerializationMethodFingerprint,
        IsUserSuperMethodFingerprint
    )
) {
    /* First find the reference to the isUserSuper field, then patch the instruction that assigns it to false.
    * This strategy is used because the method that sets the isUserSuper field is difficult to fingerprint reliably.
    */
    override fun execute(context: BytecodeContext) {
        // Find the reference to the isUserSuper field.
        val isUserSuperReference = IsUserSuperMethodFingerprint
            .result
            ?.mutableMethod
            ?.getInstructions()
            ?.filterIsInstance<BuilderInstruction22c>()
            ?.firstOrNull { it.opcode == Opcode.IGET_BOOLEAN }
            ?.reference
            ?: throw IsUserSuperMethodFingerprint.exception

        // Patch the instruction that assigns isUserSuper to true.
        UserSerializationMethodFingerprint
            .result
            ?.mutableMethod
            ?.apply {
                val assignIndex = indexOfReference(isUserSuperReference)
                val assignInstruction = getInstruction<TwoRegisterInstruction>(assignIndex)

                // add an instruction to force the value to `true`. ideally we'd replace the existing
                // instruction, but there's an `if` block above with different paths based on various
                // states (i.e. subscription vs super vs gold or whatever), and I don't think it's
                // worth removing the entire statement.
                addInstructions(
                    assignIndex + 1,
                    """
                        const/4 v${assignInstruction.registerA}, 0x1
                        iput-boolean v${assignInstruction.registerA}, v0, $isUserSuperReference
                    """.trimIndent()
                )
            }
            ?: throw UserSerializationMethodFingerprint.exception
    }

    private fun MutableMethod.indexOfReference(reference: Reference) = getInstructions()
        .indexOfFirst { it is BuilderInstruction22c && it.opcode == Opcode.IPUT_BOOLEAN && it.reference == reference }
        .let {
            if (it == -1) throw PatchException("Could not find index of instruction with supplied reference.")
            else it
        }
}