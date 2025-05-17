package app.revanced.patches.shared.misc.pairip.license

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import java.util.logging.Logger

@Suppress("unused")
val disableLicenseCheckPatch = bytecodePatch(
    name = "Disable pairip license check",
    description = "Disable Play Integrity Protect (pairip) client-side license check."
) {

    execute {
        if (processLicenseResponseFingerprint.methodOrNull == null || validateLicenseResponseFingerprint.methodOrNull == null)
            return@execute Logger.getLogger(this::class.java.name)
                .warning("Could not find pairip licensing check. No changes applied.")

        // Set first parameter (responseCode) to 0 (success status).
        processLicenseResponseFingerprint.method.addInstruction(0, "const/4 p1, 0x0")

        // Short-circuit the license response validation.
        validateLicenseResponseFingerprint.method.returnEarly();

    }
}
