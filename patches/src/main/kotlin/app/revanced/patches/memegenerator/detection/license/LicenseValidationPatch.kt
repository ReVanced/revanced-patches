package app.revanced.patches.memegenerator.detection.license

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

val licenseValidationPatch = bytecodePatch(
    description = "Disables Firebase license validation.",
) {

    apply {
        licenseValidationMethod.returnEarly(true)
    }
}
