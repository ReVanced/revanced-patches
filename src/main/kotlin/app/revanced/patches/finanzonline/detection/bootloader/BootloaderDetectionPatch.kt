package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.finanzonline.detection.bootloader.fingerprints.BootStateFingerprint
import app.revanced.patches.finanzonline.detection.bootloader.fingerprints.CreateKeyFingerprint


@Patch(
    name = "Remove bootloader detection",
    description = "Removes the check for an unlocked bootloader.",
    compatiblePackages = [CompatiblePackage("at.gv.bmf.bmf2go")]
)
@Suppress("unused")
object BootloaderDetectionPatch : BytecodePatch(
    setOf(CreateKeyFingerprint, BootStateFingerprint)
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
