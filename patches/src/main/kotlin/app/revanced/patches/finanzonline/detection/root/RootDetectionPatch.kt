package app.revanced.patches.finanzonline.detection.root

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
import app.revanced.patches.shared.PATCH_NAME_REMOVE_ROOT_DETECTION

@Suppress("unused")
val rootDetectionPatch = bytecodePatch(
    name = PATCH_NAME_REMOVE_ROOT_DETECTION,
    description = PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION,
) {
    compatibleWith("at.gv.bmf.bmf2go")

    execute {
        rootDetectionFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """,
        )
    }
}
