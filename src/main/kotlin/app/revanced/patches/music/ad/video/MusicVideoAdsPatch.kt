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
    name = "Music video ads",
    description = "Removes ads in the music player.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object MusicVideoAdsPatch : BytecodePatch(
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
