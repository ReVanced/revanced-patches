package app.revanced.patches.shared.misc.pairip.license

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableLicenseCheckPatch = bytecodePatch(
    name = "Disable license check",
    description = "Disable Play Integrity Protect (pairip) client-side license check."
) {
    
    execute {
        // Set first parameter (responseCode) to 0 (success status).
        processLicenseResponseFingerprint.method.addInstruction(0, "const/4 p1, 0x0")

        // Short-circuit the license response validation.
        validateLicenseResponseFingerprint.method.returnEarly();
    }
}
