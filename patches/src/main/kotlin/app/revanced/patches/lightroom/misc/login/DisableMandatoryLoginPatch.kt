package app.revanced.patches.lightroom.misc.login

import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Disable mandatory login` by creatingBytecodePatch {
    compatibleWith("com.adobe.lrmobile"("9.3.0"))

    apply {
        val index = isLoggedInMethod.instructions.lastIndex - 1
        // Set isLoggedIn = true.
        isLoggedInMethod.replaceInstruction(index, "const/4 v0, 0x1")
    }
}
