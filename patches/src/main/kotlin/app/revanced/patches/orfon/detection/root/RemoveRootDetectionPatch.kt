package app.revanced.patches.orfon.detection.root

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Remove root detection` by creatingBytecodePatch(
    description = "Removes the check for root permissions and unlocked bootloader."
) {
    compatibleWith("com.nousguide.android.orftvthek")

    apply {
        isDeviceRootedMethod.returnEarly(false)
    }
}
