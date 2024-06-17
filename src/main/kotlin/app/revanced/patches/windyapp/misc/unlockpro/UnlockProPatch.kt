package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks all pro features.",
) {
    compatibleWith("co.windyapp.android")

    val checkProMatch by checkProFingerprint()

    execute {
        checkProMatch.mutableMethod.addInstructions(
            0,
            """
                const/16 v0, 0x1
                return v0
            """,
        )
    }
}
