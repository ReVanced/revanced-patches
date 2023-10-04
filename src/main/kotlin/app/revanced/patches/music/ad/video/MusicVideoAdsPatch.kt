package app.revanced.patches.music.ad.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsConstructorFingerprint
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsFingerprint

@Patch(
    name = "Music video ads",
    description = "Removes ads in the music player.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object MusicVideoAdsPatch : BytecodePatch(
    setOf(ShowMusicVideoAdsConstructorFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        ShowMusicVideoAdsFingerprint.resolve(context, ShowMusicVideoAdsConstructorFingerprint.result!!.classDef)

        val result = ShowMusicVideoAdsFingerprint.result!!

        result.mutableMethod.addInstruction(
            result.scanResult.patternScanResult!!.startIndex,
            """
                const/4 p1, 0x0
            """
        )
    }
}
