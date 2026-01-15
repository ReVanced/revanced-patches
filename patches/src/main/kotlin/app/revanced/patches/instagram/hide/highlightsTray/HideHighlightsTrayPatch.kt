package app.revanced.patches.instagram.hide.highlightsTray

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.hide.explore.replaceJsonFieldWithBogus

@Suppress("unused")
val hideHighlightsTrayPatch = bytecodePatch(
    name = "Hide highlights tray",
    description = "Hides highlights tray in profile section.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        highlightsUrlBuilderFingerprint.replaceJsonFieldWithBogus(TARGET_STRING)
    }
}
