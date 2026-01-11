package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.shared.PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Remove root detection` by creatingBytecodePatch(
    description = PATCH_DESCRIPTION_REMOVE_ROOT_DETECTION
) {
    compatibleWith("com.nousguide.android.orftvthek")

    apply {
        isDeviceRootedMethod.returnEarly(false)
    }
}
