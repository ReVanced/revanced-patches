package app.revanced.patches.twitter.layout.viewcount

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideViewCountPatch = bytecodePatch(
    name = "Hide view count",
    description = "Hides the view count of Posts.",
    use = false,
) {
    compatibleWith("com.twitter.android")

    apply {
        viewCountsEnabledMethod.returnEarly()
    }
}
