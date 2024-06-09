package app.revanced.patches.messenger.inputfield.patch

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.inputfield.fingerprints.SendTypingIndicatorFingerprint
import app.revanced.patches.messenger.inputfield.fingerprints.sendTypingIndicatorFingerprint

@Suppress("unused")
val disableTypingIndicatorPatch = bytecodePatch(
    name = "Disable typing indicator",
    description = "Disables the indicator while typing a message."
) {
    compatibleWith("com.facebook.orca")

    val sendTypingIndicatorResult by sendTypingIndicatorFingerprint

    execute {
        sendTypingIndicatorResult.mutableMethod.replaceInstruction(0, "return-void")
    }
}