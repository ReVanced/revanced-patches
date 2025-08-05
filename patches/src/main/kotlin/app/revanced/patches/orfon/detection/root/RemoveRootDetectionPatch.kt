package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val removeRootDetectionPatch = bytecodePatch(
    name = "Remove root detection",
    description = "Removes the check for root permissions.",
) {
    compatibleWith("com.nousguide.android.orftvthek")

    execute {
        isDeviceRootedFingeprint.method.returnEarly(false)
    }
}