package app.revanced.patches.spotify.navbar

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.unlockPremiumPatch

@Deprecated("Superseded by unlockPremiumPatch", ReplaceWith("unlockPremiumPatch"))
@Suppress("unused")
val premiumNavbarTabPatch = bytecodePatch(
    name = "Premium navbar tab",
    description = "Hides the premium tab from the navigation bar.",
    use = false,
) {
    dependsOn(unlockPremiumPatch)
}
