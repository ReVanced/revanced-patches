package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.iconpackstudio.misc.pro.fingerprints.checkProFingerprint

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
) {
    compatibleWith("ginlemon.iconpackstudio"("2.2 build 016"))

    val checkProResult by checkProFingerprint

    execute {
        checkProResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}