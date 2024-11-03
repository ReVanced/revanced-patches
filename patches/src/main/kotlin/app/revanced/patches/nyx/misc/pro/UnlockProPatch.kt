package app.revanced.patches.nyx.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("com.awedea.nyx")

    execute {
        checkProFingerprint.matchOrThrow.method.addInstructions(
            0,
            """
                 const/4 v0, 0x1
                 return v0
            """,
        )
    }
}
