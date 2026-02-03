package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.patch.bytecodePatch

@Deprecated("This patch is useless by itself and has been merged into another patch.", ReplaceWith("unlockAndroidAutoMediaBrowserPatch"))
@Suppress("unused")
val bypassCertificateChecksPatch = bytecodePatch(
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
) {
    dependsOn(unlockAndroidAutoMediaBrowserPatch)
}
