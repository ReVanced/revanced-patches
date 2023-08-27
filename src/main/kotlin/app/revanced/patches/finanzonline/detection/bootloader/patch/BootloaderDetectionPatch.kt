package app.revanced.patches.finanzonline.detection.bootloader.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.finanzonline.detection.bootloader.fingerprints.BootStateFingerprint
import app.revanced.patches.finanzonline.detection.bootloader.fingerprints.CreateKeyFingerprint
import app.revanced.patches.finanzonline.detection.shared.annotations.DetectionCompatibility


@Patch
@Name("Remove bootloader detection")
@Description("Removes the check for an unlocked bootloader.")
@DetectionCompatibility
class BootloaderDetectionPatch : BytecodePatch(
    listOf(CreateKeyFingerprint, BootStateFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        arrayOf(CreateKeyFingerprint, BootStateFingerprint).forEach { fingerprint ->
            fingerprint.result?.mutableMethod?.addInstructions(
                0,
                """
                        const/4 v0, 0x1
                        return v0
                """
            ) ?: throw fingerprint.exception
        }
    }
}
