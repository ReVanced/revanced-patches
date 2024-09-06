package app.revanced.patches.music.ad.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.ad.video.fingerprints.ShowVideoAdsParentFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide video ads",
    description = "Hides ads that appear while listening to or streaming music videos, podcasts, or songs.",
    compatiblePackages = [
        CompatiblePackage("com.google.android.apps.youtube.music")
    ],
)
@Suppress("unused")
object HideVideoAds : BytecodePatch(
    setOf(ShowVideoAdsParentFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ShowVideoAdsParentFingerprint.result?.let {
            val showVideoAdsMethod = context
                .toMethodWalker(it.mutableMethod)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex + 1, true).getMethod() as MutableMethod

            showVideoAdsMethod.addInstruction(0, "const/4 p1, 0x0")
        } ?: throw ShowVideoAdsParentFingerprint.exception
    }
}

@Deprecated("This patch class has been renamed to HideVideoAds.")
object HideMusicVideoAds : BytecodePatch(
    dependencies = setOf(HideVideoAds::class)
) {
    override fun execute(context: BytecodeContext) {
    }
}

@Deprecated("This patch class has been renamed to HideVideoAds.")
object MusicVideoAdsPatch : BytecodePatch(
    dependencies = setOf(HideMusicVideoAds::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}