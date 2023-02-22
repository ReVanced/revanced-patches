package app.revanced.patches.photomath.detection.signature.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patches.photomath.detection.signature.annotations.DisableSignatureDetectionCompatibility
import app.revanced.patches.photomath.detection.signature.fingerprints.CheckSignatureFingerprint
import app.revanced.patches.photomath.detection.signature.fingerprints.MainOnCreateFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Description("Disables detection of incorrect signature.")
@DisableSignatureDetectionCompatibility
@Version("0.0.1")
class SignatureDetectionPatch : BytecodePatch(
    listOf(
        MainOnCreateFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val mainOnCreate = MainOnCreateFingerprint.result!!

        val patternResult = CheckSignatureFingerprint.also {
            it.resolve(context, mainOnCreate.method, mainOnCreate.classDef)
        }.result!!.scanResult.patternScanResult!!

        mainOnCreate.mutableMethod.apply {
            val signatureCheckInstruction = instruction(patternResult.endIndex)
            val checkRegister = (signatureCheckInstruction as OneRegisterInstruction).registerA

            replaceInstruction(signatureCheckInstruction.location.index, "const/4 v$checkRegister, 0x1")
        }

        return PatchResultSuccess()
    }

}