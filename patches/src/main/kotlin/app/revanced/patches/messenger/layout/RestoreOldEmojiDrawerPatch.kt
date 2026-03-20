package app.revanced.patches.messenger.layout

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.firstMethod
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val restoreOldEmojiDrawerPatch = bytecodePatch(
    name = "Restore old emoji drawer",
    description = "Disables the new redesigned emoji drawer.",
) {
    compatibleWith("com.facebook.orca")

    apply {
        val isRedesignedDrawerEnabledMethod = with(renderRedesignedDrawerMethodMatch) {
            firstMethod(it.method.getInstruction(this[0]).methodReference!!)
        }
        isRedesignedDrawerEnabledMethod.returnEarly(false)
    }
}