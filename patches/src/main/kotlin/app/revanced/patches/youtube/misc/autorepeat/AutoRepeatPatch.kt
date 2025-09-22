package app.revanced.patches.youtube.misc.autorepeat

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.loopvideo.loopVideoPatch

@Deprecated("Patch was renamed", ReplaceWith("looVideoPatch"))
val autoRepeatPatch = bytecodePatch {
    dependsOn(loopVideoPatch)
}
