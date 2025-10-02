package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
import app.revanced.patches.shared.PATCH_NAME_REMOVE_ROOT_DETECTION
import app.revanced.util.returnEarly

@Suppress("unused")
val removeRootDetectionPatch = bytecodePatch(
    name = PATCH_NAME_REMOVE_ROOT_DETECTION,
    description = PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
) {
    compatibleWith("com.nousguide.android.orftvthek")

    execute {
        isDeviceRootedFingeprint.method.returnEarly(false)
    }
}