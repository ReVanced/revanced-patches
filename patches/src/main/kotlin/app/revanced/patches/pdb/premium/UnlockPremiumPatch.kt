package app.revanced.patches.pdb.premium

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Pro features by enabling the built-in mock Pro user flag.",
) {
    compatibleWith("pdb.app"("2.108.0"))

    execute {
        // Make isMockProUser() always return true.
        // This leverages the app's built-in developer testing feature
        // which bypasses all Pro status checks in UserInfo.isProUser().
        isMockProUserFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
