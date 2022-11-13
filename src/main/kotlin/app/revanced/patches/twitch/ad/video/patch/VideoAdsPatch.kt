package app.revanced.patches.twitch.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.twitch.ad.video.annotations.VideoAdsCompatibility
import app.revanced.patches.twitch.ad.video.fingerprints.AdsManagerFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.CheckAdEligibilityLambdaFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.ContentConfigShowAdsFingerprint

@Patch
@Name("block-video-ads")
@Description("Blocks video ads in streams and VODs.")
@VideoAdsCompatibility
@Version("0.0.1")
class VideoAdsPatch : BytecodePatch(
    listOf(
        ContentConfigShowAdsFingerprint,
        AdsManagerFingerprint,
        CheckAdEligibilityLambdaFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // Pretend our player is ineligible for all ads
        with(CheckAdEligibilityLambdaFingerprint.result!!) {
            mutableMethod.addInstructions(
                0,
                """
                    const/4 v0, 0
                    invoke-static {v0}, Lio/reactivex/Single;->just(Ljava/lang/Object;)Lio/reactivex/Single;
                    move-result-object p0
                    return-object p0
                """
            )
        }

        // Spoof showAds JSON field
        with(ContentConfigShowAdsFingerprint.result!!) {
            mutableMethod.addInstructions(0, """
                    const/4 v0, 0
                    return v0
                """
            )
        }

        // Block playAds call
        with(AdsManagerFingerprint.result!!) {
            mutableMethod.addInstruction(0, "return-void")
        }

        return PatchResultSuccess()
    }
}
