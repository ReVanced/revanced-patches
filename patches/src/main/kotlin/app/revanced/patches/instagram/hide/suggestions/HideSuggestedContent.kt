package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.instagram.hide.explore.replaceJsonFieldWithBogus

@Suppress("unused")
val `Hide suggested content` by creatingBytecodePatch(
    description = "Hides suggested stories, reels, threads and survey from feed (Suggested posts will still be shown).",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        FEED_ITEM_KEYS_TO_BE_HIDDEN.forEach { key ->
            feedItemParseFromJsonFingerprint.replaceJsonFieldWithBogus(key)
        }
    }
}
