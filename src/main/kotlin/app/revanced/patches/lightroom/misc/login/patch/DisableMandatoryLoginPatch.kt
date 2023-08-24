package app.revanced.patches.lightroom.misc.login.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.lightroom.misc.login.annotations.DisableMandatoryLoginCompatibility
import app.revanced.patches.lightroom.misc.login.fingerprint.IsLoggedInFingerprint

@Patch
@Name("Disable mandatory login")
@DisableMandatoryLoginCompatibility
class DisableMandatoryLoginPatch : BytecodePatch(listOf(IsLoggedInFingerprint)) {
    override fun execute(context: BytecodeContext) {
        IsLoggedInFingerprint.result?.mutableMethod?.apply {
            val index = implementation!!.instructions.lastIndex - 1
            // Set isLoggedIn = true.
            replaceInstruction(index, "const/4 v0, 0x1")
        } ?: throw IsLoggedInFingerprint.exception
    }
}