package app.revanced.patches.photomath.misc.unlock.bookpoint

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.matchOrThrow

val enableBookpointPatch = bytecodePatch(
    description = "Enables textbook access",
) {

    execute {
        isBookpointEnabledFingerprint.matchOrThrow.method.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
