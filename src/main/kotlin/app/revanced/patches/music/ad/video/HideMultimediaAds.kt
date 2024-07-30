package app.revanced.patches.music.ad.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.ad.video.fingerprints.ShowMultimediaAdsParentFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide multimedia ads",
    description = "Hides ads played when listening to or watching music videos, podcasts and songs.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.45.54",
                "6.51.53",
                "7.01.53",
                "7.02.52",
                "7.03.52",
            ]
        )
    ],
)
@Suppress("unused")
object HideMultimediaAds : BytecodePatch(
    setOf(ShowMultimediaAdsParentFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ShowMultimediaAdsParentFingerprint.result?.let {
            val showMultimediaAdsMethod = context
                .toMethodWalker(it.mutableMethod)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex + 1, true).getMethod() as MutableMethod

            showMultimediaAdsMethod.addInstruction(0, "const/4 p1, 0x0")
        } ?: throw ShowMultimediaAdsParentFingerprint.exception
    }
}

@Deprecated("This patch class has been renamed to HideMultimediaAds.")
object MusicVideoAdsPatch : BytecodePatch(
    dependencies = setOf(HideMultimediaAds::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
