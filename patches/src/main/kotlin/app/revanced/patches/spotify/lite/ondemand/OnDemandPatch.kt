package app.revanced.patches.spotify.lite.ondemand

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Patch no longer works and will be deleted soon")
@Suppress("unused")
val onDemandPatch = bytecodePatch(
    description = "Enables listening to songs on-demand, allowing to play any song from playlists, albums or artists without limitations. This does not remove ads.",
)
