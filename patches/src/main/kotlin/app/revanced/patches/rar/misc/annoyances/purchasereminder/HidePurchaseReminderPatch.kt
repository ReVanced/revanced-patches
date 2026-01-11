package app.revanced.patches.rar.misc.annoyances.purchasereminder

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Hide purchase reminder` by creatingBytecodePatch(
    description = "Hides the popup that reminds you to purchase the app."
) {
    compatibleWith("com.rarlab.rar")

    apply {
        showReminderMethod.addInstruction(0, "return-void")
    }
}
