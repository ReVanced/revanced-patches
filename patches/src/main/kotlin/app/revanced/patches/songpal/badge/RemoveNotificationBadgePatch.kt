package app.revanced.patches.songpal.badge

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val removeNotificationBadgePatch = bytecodePatch(
    name = "Remove notification badge",
    description = "Removes the red notification badge from the activity tab.",
) {
    compatibleWith("com.sony.songpal.mdr"("10.1.0"))

    execute {
        showNotificationFingerprint.method.addInstructions(0, "return-void")
    }
}
