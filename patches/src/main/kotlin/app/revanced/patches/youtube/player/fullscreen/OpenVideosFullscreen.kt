package app.revanced.patches.youtube.player.fullscreen

import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
@Deprecated("Renamed to openVideosFullscreenPatch", ReplaceWith("openVideosFullscreenPatch"))
val openVideosFullscreen = bytecodePatch{
    dependsOn(openVideosFullscreenPatch)
}