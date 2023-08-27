package app.revanced.patches.memegenerator.detection.license.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.memegenerator.detection.license.fingerprint.LicenseValidationFingerprint

@Description("Disables Firebase license validation.")
class LicenseValidationPatch : BytecodePatch(
    listOf(LicenseValidationFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        LicenseValidationFingerprint.result?.apply {
            mutableMethod.replaceInstructions(
                0,
                """
                    const/4 p0, 0x1
                    return  p0
                """
            )
        } ?: throw LicenseValidationFingerprint.exception
    }
}