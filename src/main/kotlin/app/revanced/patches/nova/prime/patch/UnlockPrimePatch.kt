package app.revanced.patches.nova.prime.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.nova.prime.annotations.UnlockPrimeCompatibility
import app.revanced.patches.nova.prime.fingerprints.UnlockPrimeFingerprint
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x

@Patch
@Name("unlock-prime")
@Description("Unlocks Nova Prime and all functions of the app.")
@UnlockPrimeCompatibility
@Version("0.0.1")
class UnlockPrimePatch : BytecodePatch(
    listOf(
        UnlockPrimeFingerprint
    )
) {
    private companion object {
        // Any value except 0 unlocks prime, but 512 is needed for a protection mechanism
        // which would reset the preferences if the value on disk had changed after a restart.
        const val PRIME_STATUS: Int = 512
    }

    override fun execute(context: BytecodeContext): PatchResult {
        UnlockPrimeFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.endIndex + 1

            val primeStatusRegister =
                (mutableMethod.implementation!!.instructions[insertIndex - 1] as BuilderInstruction11x).registerA

            mutableMethod.addInstruction(
                insertIndex,
                """
                    const/16 v$primeStatusRegister, $PRIME_STATUS
                """
            )
        } ?: UnlockPrimeFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
