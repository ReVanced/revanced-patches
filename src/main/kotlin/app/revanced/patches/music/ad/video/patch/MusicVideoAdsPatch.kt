package app.revanced.patches.music.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsConstructorFingerprint
import app.revanced.patches.music.ad.video.fingerprints.ShowMusicVideoAdsFingerprint
import app.revanced.patches.music.annotations.MusicCompatibility

@Patch
@Name("Music video ads")
@Description("Removes ads in the music player.")
@MusicCompatibility
class MusicVideoAdsPatch : BytecodePatch(
    listOf(ShowMusicVideoAdsConstructorFingerprint)
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
