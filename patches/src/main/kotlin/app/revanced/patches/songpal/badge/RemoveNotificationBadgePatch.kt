package app.revanced.patches.songpal.badge

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Remove notification badge` by creatingBytecodePatch(
    description = "Removes the red notification badge from the activity tab.",
) {
    compatibleWith("com.sony.songpal.mdr"("10.1.0"))

    apply {
        showNotificationMethod.addInstructions(0, "return-void")
    }
}
