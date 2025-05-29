package app.revanced.patches.instagram.ads

import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideAdsPatch = bytecodePatch {
    dependsOn(app.revanced.patches.meta.ads.hideAdsPatch)
}
