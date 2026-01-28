package app.revanced.patches.shared.misc.pairip.license

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly
import java.util.logging.Logger

@Suppress("unused", "ObjectPropertyName")
val `Disable Pairip license check` by creatingBytecodePatch(
    description = "Disables Play Integrity API (Pairip) client-side license check.",
    use = false,
) {

    apply {
        if (processLicenseResponseMethod == null || validateLicenseResponseMethod == null) {
            return@apply Logger.getLogger(this::class.java.name)
                .warning("Could not find Pairip licensing check. No changes applied.")
        }

        // Set first parameter (responseCode) to 0 (success status).
        processLicenseResponseMethod!!.addInstruction(0, "const/4 p1, 0x0")

        // Short-circuit the license response validation.
        validateLicenseResponseMethod!!.returnEarly()
    }
}
