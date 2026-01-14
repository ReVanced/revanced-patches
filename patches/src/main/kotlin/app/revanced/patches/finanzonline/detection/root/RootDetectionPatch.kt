package app.revanced.patches.finanzonline.detection.root

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Remove root detection` by creatingBytecodePatch(
    description = "Removes the check for root permissions and unlocked bootloader.",
) {
    compatibleWith("at.gv.bmf.bmf2go")

    apply {
        rootDetectionFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """,
        )
    }
}
