package app.revanced.patches.photomath.misc.unlock.bookpoint

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val enableBookpointPatch = bytecodePatch(
    description = "Enables textbook access",
) {
    val isBookpointEnabledFingerprintResult by isBookpointEnabledFingerprint

    execute {
        isBookpointEnabledFingerprintResult.mutableMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
