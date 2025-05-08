package app.revanced.patches.angulus.misc.license

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableLicenseCheckPatch = bytecodePatch(
    name = "Disable license check",
    description = "Disable client-side license check to prevent app from closing on startup."
) {
    compatibleWith("com.drinkplusplus.angulus"("5.0.20"))

    execute {
        // Set first parameter (responseCode) to 0.
        processLicenseResponseFingerprint.method.addInstruction(0, "const/4 p1, 0x0")

        // Short-circuit the license response validation.
        validateLicenseResponseFingerprint.method.returnEarly();
    }
}
