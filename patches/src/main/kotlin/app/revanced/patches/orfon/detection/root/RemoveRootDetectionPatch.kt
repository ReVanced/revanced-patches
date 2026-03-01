package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeRootDetectionPatch = bytecodePatch(
    name = "Remove root detection",
    description = "Removes the check for root permissions and unlocked bootloader.",
) {
    compatibleWith("com.nousguide.android.orftvthek")

    apply {
        isDeviceRootedMethod.returnEarly(false)
    }
}
