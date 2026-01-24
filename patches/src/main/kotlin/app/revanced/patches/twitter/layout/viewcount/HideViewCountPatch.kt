package app.revanced.patches.twitter.layout.viewcount

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Hide view count` by creatingBytecodePatch(
    description = "Hides the view count of Posts.",
    use = false,
) {
    compatibleWith("com.twitter.android")

    apply {
        viewCountsEnabledMethod.returnEarly()
    }
}
