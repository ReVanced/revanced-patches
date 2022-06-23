package app.revanced.patches.youtube.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility
import app.revanced.patches.youtube.ad.video.signatures.ShowVideoAdsConstructorSignature
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.AccessFlags

@Patch
@Dependencies(dependencies = [IntegrationsPatch::class])
@Name("video-ads")
@Description("Patch to remove ads in the YouTube video player.")
@VideoAdsCompatibility
@Version("0.0.1")
class VideoAdsPatch : BytecodePatch(
    listOf(
        ShowVideoAdsConstructorSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result =
            ShowVideoAdsConstructorSignature.result!!.findParentMethod(@Name("show-video-ads-method-signature") @MatchingMethod(
                definingClass = "zai"
            ) @DirectPatternScanMethod @VideoAdsCompatibility @Version("0.0.1") object : MethodSignature(
                "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("Z"), null
            ) {}) ?: return PatchResultError("Required parent method could not be found.")

        // Override the parameter by calling shouldShowAds and setting the parameter to the result
        result.method.addInstructions(
            0, """
                invoke-static { }, Lfi/vanced/libraries/youtube/whitelisting/Whitelist;->shouldShowAds()Z
                move-result v1
            """
        )

        return PatchResultSuccess()
    }
}