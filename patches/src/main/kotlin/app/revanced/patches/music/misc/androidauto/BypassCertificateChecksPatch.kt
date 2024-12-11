package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val bypassCertificateChecksPatch = bytecodePatch(
    name = "Bypass certificate checks",
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
) {
    compatibleWith("com.google.android.apps.youtube.music"("7.29.52"))

    execute {
        checkCertificateFingerprint.method.returnEarly(true)
    }
}
