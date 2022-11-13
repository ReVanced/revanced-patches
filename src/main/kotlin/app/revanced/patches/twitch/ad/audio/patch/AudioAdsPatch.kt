package app.revanced.patches.twitch.ad.audio.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twitch.ad.audio.annotations.AudioAdsCompatibility
import app.revanced.patches.twitch.ad.audio.fingerprints.AudioAdsPresenterPlayFingerprint

@Patch
@Name("block-audio-ads")
@Description("Blocks audio ads in streams and VODs.")
@AudioAdsCompatibility
@Version("0.0.1")
class AudioAdsPatch : BytecodePatch(
    listOf(AudioAdsPresenterPlayFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // Block playAds call
        with(AudioAdsPresenterPlayFingerprint.result!!) {
            mutableMethod.addInstruction(0, "return-void")
        }

        return PatchResultSuccess()
    }
}
