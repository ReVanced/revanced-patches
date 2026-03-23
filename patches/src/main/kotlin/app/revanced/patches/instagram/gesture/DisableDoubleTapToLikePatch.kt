package app.revanced.patches.instagram.gesture

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableDoubleTapToLikePatch = bytecodePatch(
    name = "Disable double-tap to like",
    description = "Disables the double-tap gesture to like posts in the feed.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        doubleTapToLikeMethod.returnEarly()
    }
}
