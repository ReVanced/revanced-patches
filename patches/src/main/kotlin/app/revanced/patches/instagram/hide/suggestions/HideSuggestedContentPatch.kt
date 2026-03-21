package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideSuggestedContentPatch = bytecodePatch(
    name = "Hide suggested content",
    description = "Hides suggested stories, reels, threads and survey from feed.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        // Return early from feed item parsing to skip suggested content
        feedItemParseFromJsonMethod.returnEarly()
    }
}
