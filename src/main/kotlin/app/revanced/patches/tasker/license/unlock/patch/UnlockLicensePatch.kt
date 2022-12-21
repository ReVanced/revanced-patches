package app.revanced.patches.tasker.license.unlock.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tasker.license.unlock.annotations.UnlockLicenseCompatibility
import app.revanced.patches.tasker.license.unlock.fingerprints.CheckLicenseFingerprint
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c

@Patch
@Name("unlock-license")
@Description("Unlocks the trial version.")
@UnlockLicenseCompatibility
@Version("0.0.1")
class UnlockLicensePatch : BytecodePatch(
    listOf(
        CheckLicenseFingerprint
    )
) {
    override fun execute(context: BytecodeContext) = CheckLicenseFingerprint.result?.let { result ->
        val patchIndex = result.scanResult.patternScanResult!!.endIndex

        with(result.mutableMethod.instruction(patchIndex) as FiveRegisterInstruction) {
            ImmutableInstruction35c(
                opcode,
                registerCount,
                registerC,
                0, // registerE is 1, registerD is now 0 instead of 1 bypassing the license verification
                registerE,
                registerF,
                registerG,
                (this as ReferenceInstruction).reference as MethodReference
            )
        }
        PatchResultSuccess()
    } ?: CheckLicenseFingerprint.toErrorResult()
}