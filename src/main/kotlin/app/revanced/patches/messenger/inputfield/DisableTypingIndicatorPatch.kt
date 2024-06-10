package app.revanced.patches.messenger.inputfield

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.inputfield.fingerprints.sendTypingIndicatorFingerprint

@Suppress("unused")
val disableTypingIndicatorPatch = bytecodePatch(
    name = "Disable typing indicator",
    description = "Disables the indicator while typing a message.",
) {
    compatibleWith("com.facebook.orca")

    val sendTypingIndicatorResult by sendTypingIndicatorFingerprint

    execute {
        sendTypingIndicatorResult.mutableMethod.replaceInstruction(0, "return-void")
    }
}
