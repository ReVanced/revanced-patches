package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val bootloaderDetectionPatch = bytecodePatch(
    name = "Remove bootloader detection",
    description = "Removes the check for an unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    val createKeyFingerprintResult by createKeyFingerprint()
    val bootStateFingerprintResult by bootStateFingerprint()

    execute {
        setOf(createKeyFingerprintResult, bootStateFingerprintResult).forEach { result ->
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
