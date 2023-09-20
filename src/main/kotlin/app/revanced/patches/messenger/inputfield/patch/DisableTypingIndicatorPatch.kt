package app.revanced.patches.messenger.inputfield.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.messenger.inputfield.fingerprints.SendTypingIndicatorFingerprint

@Patch(
    name = "Disable typing indicator",
    description = "Disables the indicator while typing a message.",
    compatiblePackages = [CompatiblePackage("com.facebook.orca")]
)
@Suppress("unused")
object DisableTypingIndicatorPatch : BytecodePatch(
    setOf(SendTypingIndicatorFingerprint)
){
    override fun execute(context: BytecodeContext) {
        SendTypingIndicatorFingerprint.result?.mutableMethod?.replaceInstruction(0, "return-void")
            ?: throw SendTypingIndicatorFingerprint.exception
    }
}
