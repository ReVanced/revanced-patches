package app.revanced.patches.nyx.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Deprecated("This patch will be removed in the future.")
@Suppress("unused")
val unlockProPatch = bytecodePatch {
    compatibleWith("com.awedea.nyx")

    execute {
        checkProFingerprint.method.addInstructions(
            0,
            """
                 const/4 v0, 0x1
                 return v0
            """,
        )
    }
}
