package app.revanced.patches.lightroom.misc.login

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableMandatoryLoginPatch = bytecodePatch(
    name = "Disable mandatory login",
) {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    execute {
        isLoggedInFingerprint.method.apply {
            val index = implementation!!.instructions.lastIndex - 1
            // Set isLoggedIn = true.
            replaceInstruction(index, "const/4 v0, 0x1")
        }
    }
}
