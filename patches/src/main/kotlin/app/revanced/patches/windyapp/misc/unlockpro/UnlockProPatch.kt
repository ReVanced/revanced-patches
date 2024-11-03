package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks all pro features.",
) {
    compatibleWith("co.windyapp.android")

    execute {
        checkProFingerprint.matchOrThrow.method.addInstructions(
            0,
            """
                const/16 v0, 0x1
                return v0
            """,
        )
    }
}
