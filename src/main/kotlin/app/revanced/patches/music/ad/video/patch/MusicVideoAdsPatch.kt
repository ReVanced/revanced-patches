package app.revanced.patches.music.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.music.ad.video.annotations.MusicVideoAdsCompatibility
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsConstructorFingerprint
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsFingerprint

@Patch
@Name("music-video-ads")
@Description("Removes ads in the YouTube Music player.")
@MusicVideoAdsCompatibility
@Version("0.0.1")
class MusicVideoAdsPatch : BytecodePatch(
    listOf(
        ShowMusicVideoAdsConstructorFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        ShowMusicVideoAdsFingerprint.resolve(data, ShowMusicVideoAdsConstructorFingerprint.result!!.classDef)

        val result = ShowMusicVideoAdsFingerprint.result!!

        result.mutableMethod.addInstructions(
            result.patternScanResult!!.startIndex, """
                const/4 p1, 0x0
            """
        )

        return PatchResultSuccess()
    }
}
