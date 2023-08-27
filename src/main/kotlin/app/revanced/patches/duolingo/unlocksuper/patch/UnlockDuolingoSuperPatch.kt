package app.revanced.patches.duolingo.unlocksuper.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.duolingo.unlocksuper.fingerprints.IsUserSuperMethodFingerprint
import app.revanced.patches.duolingo.unlocksuper.fingerprints.UserSerializationMethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c
import com.android.tools.smali.dexlib2.iface.reference.Reference

@Patch
@Name("Unlock Duolingo Super")
@Description("Unlocks Duolingo Super features.")
@Compatibility([Package("com.duolingo")])
class UnlockDuolingoSuperPatch : BytecodePatch(
    listOf(UserSerializationMethodFingerprint, IsUserSuperMethodFingerprint)
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
                replaceInstructions(
                    indexOfReference(isUserSuperReference) - 1,
                    "const/4 v2, 0x1"
                )
            }
            ?: throw UserSerializationMethodFingerprint.exception
    }

    private companion object {
        private fun MutableMethod.indexOfReference(reference: Reference) = getInstructions()
            .filterIsInstance<BuilderInstruction22c>()
            .filter { it.opcode == Opcode.IPUT_BOOLEAN }.indexOfFirst { it.reference == reference }.let {
                if (it == -1) throw PatchException("Could not find index of instruction with supplied reference.")
                else it
            }
    }
}