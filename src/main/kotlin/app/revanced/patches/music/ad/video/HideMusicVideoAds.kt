package app.revanced.patches.music.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.ad.video.fingerprints.showMusicVideoAdsParentFingerprint

@Suppress("unused")
val hideMusicVideoAdsPatch = bytecodePatch(
    name = "Hide music video ads",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    val showMusicVideoAdsParentResult by showMusicVideoAdsParentFingerprint

    execute { context ->
        showMusicVideoAdsParentResult.let {
            val showMusicVideoAdsMethod = context
                .navigate(it.mutableMethod)
                .at(it.scanResult.patternScanResult!!.startIndex + 1).mutable()

            showMusicVideoAdsMethod.addInstruction(0, "const/4 p1, 0x0")
        }
    }
}
