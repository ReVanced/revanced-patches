package app.revanced.patches.music.layout.doubletapbackground

import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.overlaybackground.AbstractOverlayBackgroundPatch

@Patch(
    name = "Hide double tap overlay filter",
    description = "Removes the dark overlay when double-tapping to seek.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
    use = false
)
@Suppress("unused")
object DoubleTapOverlayBackgroundPatch : AbstractOverlayBackgroundPatch(
    arrayOf("quick_seek_overlay.xml"),
    arrayOf("tap_bloom_view", "dark_background")
)