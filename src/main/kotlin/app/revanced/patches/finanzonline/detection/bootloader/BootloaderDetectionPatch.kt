package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.finanzonline.detection.bootloader.fingerprints.bootStateFingerprint
import app.revanced.patches.finanzonline.detection.bootloader.fingerprints.createKeyFingerprint

@Suppress("unused")
val bootloaderDetectionPatch = bytecodePatch(
    name = "Remove bootloader detection",
    description = "Removes the check for an unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    val createKeyResult by createKeyFingerprint
    val bootStateResult by bootStateFingerprint

    execute {
        setOf(createKeyResult, bootStateResult).forEach { result ->
            result.mutableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
