package app.revanced.patches.music.layout.doubletapbackground

import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.overlaybackground.AbstractOverlayBackgroundPatch

@Patch(
    name = "Hide player overlay filter",
    description = "Removes the dark overlay when single-tapping player.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
    use = false
)
@Suppress("unused")
object PlayerOverlayFilterPatch : AbstractOverlayBackgroundPatch(
    arrayOf("music_controls_overlay.xml"),
    arrayOf("player_control_screen")
)