package app.revanced.patches.finanzonline.detection.bootloader

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Remove bootloader detection` by creatingBytecodePatch(
    description = "Removes the check for an unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    apply {
        setOf(createKeyMethod, bootStateMethod).forEach { fingerprint ->
            fingerprint.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
