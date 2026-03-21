package app.revanced.patches.instagram.feed.scroll

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val keepScrollPositionPatch = bytecodePatch(
    name = "Keep scroll position",
    description = "Prevents the feed from automatically scrolling to the top when returning to the app.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        scrollToTopMethod.returnEarly()
    }
}
