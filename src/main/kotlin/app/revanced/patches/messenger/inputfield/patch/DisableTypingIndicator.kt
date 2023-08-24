package app.revanced.patches.messenger.inputfield.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.messenger.inputfield.fingerprints.SendTypingIndicatorFingerprint

@Patch
@Name("Disable typing indicator")
@Description("Disables the indicator while typing a message")
@Compatibility([Package("com.facebook.orca")])
class DisableTypingIndicator : BytecodePatch(listOf(SendTypingIndicatorFingerprint)) {
    override fun execute(context: BytecodeContext) {
        SendTypingIndicatorFingerprint.result?.mutableMethod?.replaceInstruction(0, "return-void")
            ?: throw SendTypingIndicatorFingerprint.exception
    }
}
