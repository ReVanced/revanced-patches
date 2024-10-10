package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val bootloaderDetectionPatch = bytecodePatch(
    name = "Remove bootloader detection",
    description = "Removes the check for an unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    val createKeyMatch by createKeyFingerprint()
    val bootStateMatch by bootStateFingerprint()

    execute {
        setOf(createKeyMatch, bootStateMatch).forEach { match ->
            match.mutableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
