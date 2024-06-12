package app.revanced.patches.music.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideMusicVideoAdsPatch = bytecodePatch(
    name = "Hide music video ads",
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    val showMusicVideoAdsParentFingerprintResult by showMusicVideoAdsParentFingerprint

    execute { context ->
        val showMusicVideoAdsMethod = context
            .navigate(showMusicVideoAdsParentFingerprintResult.mutableMethod)
            .at(showMusicVideoAdsParentFingerprintResult.scanResult.patternScanResult!!.startIndex + 1).mutable()

        showMusicVideoAdsMethod.addInstruction(0, "const/4 p1, 0x0")
    }
}
