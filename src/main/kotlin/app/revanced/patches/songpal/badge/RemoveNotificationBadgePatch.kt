package app.revanced.patches.songpal.badge

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val removeNotificationBadgePatch = bytecodePatch(
    name = "Remove notification badge",
    description = "Removes the red notification badge from the activity tab.",
) {
    compatibleWith("com.sony.songpal.mdr"("10.1.0"))

    val showNotificationMatch by showNotificationFingerprint()

    execute {
        showNotificationMatch.mutableMethod.addInstructions(0, "return-void")
    }
}
