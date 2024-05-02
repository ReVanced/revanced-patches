package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.windyapp.misc.unlockpro.fingerprints.checkProFingerprint

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks all pro features.",
) {
    compatibleWith("co.windyapp.android"())

    val checkProResult by checkProFingerprint

    execute {
        checkProResult.mutableMethod.addInstructions(
            0,
            """
                const/16 v0, 0x1
                return v0
            """,
        )
    }
}
