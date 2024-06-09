package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.androidauto.fingerprints.checkCertificateFingerprint

@Suppress("unused")
val bypassCertificateChecksPatch = bytecodePatch(
    name = "Bypass certificate checks",
    description = "Bypasses certificate checks which prevent YouTube Music from working on Android Auto.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    val checkCertificateResult by checkCertificateFingerprint

    execute {
        checkCertificateResult.mutableMethod.addInstructions(
            0,
            """
                    const/4 v0, 0x1
                    return v0
                """,
        )
    }
}
