package app.revanced.patches.spotify.navbar

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Obsolete and will be deleted soon")
@Suppress("unused")
val premiumNavbarTabPatch = bytecodePatch(
    description = "Hides the premium tab from the navigation bar.",
)
