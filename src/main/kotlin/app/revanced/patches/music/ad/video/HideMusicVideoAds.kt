package app.revanced.patches.music.ad.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsParentFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide music video ads",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object HideMusicVideoAds : BytecodePatch(
    setOf(ShowMusicVideoAdsParentFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ShowMusicVideoAdsParentFingerprint.result?.let {
            val showMusicVideoAdsMethod = context
                .toMethodWalker(it.mutableMethod)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex + 1, true).getMethod() as MutableMethod

            showMusicVideoAdsMethod.addInstruction(0, "const/4 p1, 0x0")
        } ?: throw ShowMusicVideoAdsParentFingerprint.exception
    }
}

@Deprecated("This patch class has been renamed to HideMusicVideoAds.")
object MusicVideoAdsPatch : BytecodePatch(
    dependencies = setOf(HideMusicVideoAds::class),
) {
    override fun execute(context: BytecodeContext) {
    }
}
