package app.revanced.patches.twitter.layout.viewcount

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Hide view count` by creatingBytecodePatch(
    description = "Hides the view count of Posts.",
    use = false,
) {
    compatibleWith("com.twitter.android")

    apply {
        viewCountsEnabledMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
