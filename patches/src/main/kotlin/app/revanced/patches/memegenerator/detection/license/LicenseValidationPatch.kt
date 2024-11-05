package app.revanced.patches.memegenerator.detection.license

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

val licenseValidationPatch = bytecodePatch(
    description = "Disables Firebase license validation.",
) {

    execute {
        licenseValidationFingerprint.method.replaceInstructions(
            0,
            """
                const/4 p0, 0x1
                return  p0
            """,
        )
    }
}
