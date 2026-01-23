package app.revanced.patches.twitter.layout.viewcount

import app.revanced.util.returnEarly
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Hide view count` by creatingBytecodePatch(
    description = "Hides the view count of Posts.",
    use = false,
) {
    compatibleWith("com.twitter.android")

    apply {
        viewCountsEnabledMethod.returnEarly()
    }
}
