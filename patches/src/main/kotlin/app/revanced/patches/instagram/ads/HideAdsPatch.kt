package app.revanced.patches.instagram.ads

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("Patch was moved to different package: app.revanced.patches.meta.ads.hideAdsPatch")
@Suppress("unused")
val hideAdsPatch = bytecodePatch {
    dependsOn(app.revanced.patches.meta.ads.hideAdsPatch)
}
