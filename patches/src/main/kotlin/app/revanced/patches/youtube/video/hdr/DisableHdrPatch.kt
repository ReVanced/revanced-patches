package app.revanced.patches.youtube.video.hdr

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.video.codecs.disableVideoCodecsPatch

@Deprecated("Patch was renamed", ReplaceWith("disableVideoCodecsPatch"))
@Suppress("unused")
val disableHdrPatch = bytecodePatch{
    dependsOn(disableVideoCodecsPatch)
}