package app.revanced.patches.instagram.reels.autoscroll

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableReelsAutoScrollPatch = bytecodePatch(
    name = "Disable Reels auto-scroll",
    description = "Removes the auto-scroll toggle and prevents Reels from scrolling automatically.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        // Prevent the auto-scroll feature from being initialized.
        // When this returns false, ClipsViewerFragment skips creating the auto-scroller.
        clipsAutoScrollFeatureCheckMethod.returnEarly()

        // Make the toggle button handler a no-op so tapping it does nothing.
        clipsAutoScrollToggleMethod.returnEarly()
    }
}
