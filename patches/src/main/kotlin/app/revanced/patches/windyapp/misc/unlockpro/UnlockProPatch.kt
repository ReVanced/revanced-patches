package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Deprecated("This patch no longer works and will be removed in the future.")
@Suppress("unused")
val unlockProPatch = bytecodePatch(
    description = "Unlocks all pro features.",
) {
    compatibleWith("co.windyapp.android")

    execute {
        checkProFingerprint.method.addInstructions(
            0,
            """
                const/16 v0, 0x1
                return v0
            """,
        )
    }
}
