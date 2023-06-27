package app.revanced.patches.scbeasy.detection.debugging.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.scbeasy.detection.debugging.annotations.RemoveDebuggingDetectionCompatibility
import app.revanced.patches.scbeasy.detection.debugging.fingerprints.DebuggingDetectionFingerprint

@Patch(false)
@Name("remove-debugging-detection")
@Description("Removes the USB and wireless debugging checks.")
@RemoveDebuggingDetectionCompatibility
@Version("0.0.1")
class RemoveDebuggingDetectionPatch : BytecodePatch(
    listOf(DebuggingDetectionFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        DebuggingDetectionFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
        return PatchResultSuccess()
    }
}
