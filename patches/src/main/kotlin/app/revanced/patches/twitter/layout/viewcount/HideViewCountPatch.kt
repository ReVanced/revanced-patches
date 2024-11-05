package app.revanced.patches.twitter.layout.viewcount

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide view count",
    description = "Hides the view count of Posts.",
    use = false,
) {
    compatibleWith("com.twitter.android")

    execute {
        viewCountsEnabledFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
