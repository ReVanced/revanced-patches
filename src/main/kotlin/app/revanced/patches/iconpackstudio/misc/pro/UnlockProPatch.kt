package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("ginlemon.iconpackstudio"("2.2 build 016"))

    val checkProMatch by checkProFingerprint()

    execute {
        checkProMatch.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
