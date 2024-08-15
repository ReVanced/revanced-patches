package app.revanced.patches.scbeasy.detection.debugging

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.scbeasy.detection.debugging.fingerprints.DebuggingDetectionFingerprint

@Patch(
    use = false,
    description = "Removes the USB and wireless debugging checks.",
    compatiblePackages = [CompatiblePackage("com.scb.phone")]
)
@Suppress("unused")
@Deprecated("This patch no longer work and will be removed in the future " +
        "due to the complexity of the application.\n" +
        "See https://github.com/ReVanced/revanced-patches/issues/3517 for more details.")
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
