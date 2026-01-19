package app.revanced.patches.instagram.hide.highlightsTray

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.shared.replaceStringWithBogus

@Suppress("unused")
val hideHighlightsTrayPatch = bytecodePatch(
    name = "Hide highlights tray",
    description = "Hides the highlights tray in profile section.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        highlightsUrlBuilderFingerprint.replaceStringWithBogus(TARGET_STRING)
    }
}
