package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Patch made obsolete by a new in-app setting")
@Suppress("unused")
val hideCreateButtonPatch = bytecodePatch(
    description = "Hides the \"Create\" button in the navigation bar."
) {}
