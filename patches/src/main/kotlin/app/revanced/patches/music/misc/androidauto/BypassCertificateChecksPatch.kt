package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.util.returnEarly

@Deprecated("This patch is useless by itself and has been merged into another patch.", ReplaceWith("unlockAndroidAutoMediaBrowserPatch"))

@Suppress("unused")
val bypassCertificateChecksPatch = bytecodePatch(
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
) {
    dependsOn(unlockAndroidAutoMediaBrowserPatch)
}
