package app.revanced.patches.instagram.feed.autorefresh

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableFeedAutoRefreshPatch = bytecodePatch(
    name = "Disable feed auto-refresh",
    description = "Disables automatic feed refresh when returning to the app or scrolling to top.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        pullToRefreshMethod.returnEarly()
    }
}
