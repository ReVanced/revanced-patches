package app.revanced.patches.lightroom.misc.login

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.lightroom.misc.login.fingerprints.IsLoggedInFingerprint

@Patch(
    name = "Disable mandatory login",
    compatiblePackages = [CompatiblePackage("com.adobe.lrmobile")]
)
@Suppress("unused")
object DisableMandatoryLoginPatch : BytecodePatch(
    setOf(IsLoggedInFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        IsLoggedInFingerprint.result?.mutableMethod?.apply {
            val index = implementation!!.instructions.lastIndex - 1
            // Set isLoggedIn = true.
            replaceInstruction(index, "const/4 v0, 0x1")
        } ?: throw IsLoggedInFingerprint.exception
    }
}