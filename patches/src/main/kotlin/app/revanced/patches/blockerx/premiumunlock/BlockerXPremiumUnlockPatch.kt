package app.revanced.patches.blockerx.premiumunlock

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val blockerXPremiumUnlockPatch = bytecodePatch(
    name = "Unlock Premium Features",
    description = "Unlocks premium features and removes ads by making getSUB_STATUS() always return true",
) {
    compatibleWith("io.funswitch.blocker")

    execute {
        getSubStatusFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}

