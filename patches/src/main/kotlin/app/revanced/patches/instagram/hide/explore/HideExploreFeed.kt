package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.shared.replaceStringWithBogus

@Suppress("unused")
val hideExploreFeedPatch = bytecodePatch(
    name = "Hide explore feed",
    description = "Hides posts and reels from the explore/search page.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    execute {
        exploreResponseJsonParserFingerprint.replaceStringWithBogus(EXPLORE_KEY_TO_BE_HIDDEN)
    }
}
