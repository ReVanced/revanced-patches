package app.revanced.patches.nyx.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("com.awedea.nyx")

    val checkProResult by checkProFingerprint

    execute {
        checkProResult.mutableMethod.addInstructions(
            0,
            """
                 const/4 v0, 0x1
                 return v0
            """,
        )
    }
}