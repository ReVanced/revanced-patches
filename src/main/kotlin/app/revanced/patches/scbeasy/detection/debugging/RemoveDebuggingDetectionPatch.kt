package app.revanced.patches.scbeasy.detection.debugging

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.scbeasy.detection.debugging.fingerprints.DebuggingDetectionFingerprint

@Patch(
    use = false,
    name = "Remove debugging detection",
    description = "Removes the USB and wireless debugging checks.",
    compatiblePackages = [CompatiblePackage("com.scb.phone")]
)
@Suppress("unused")
object RemoveDebuggingDetectionPatch : BytecodePatch(
    setOf(DebuggingDetectionFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        DebuggingDetectionFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        ) ?: throw DebuggingDetectionFingerprint.exception
    }
}
