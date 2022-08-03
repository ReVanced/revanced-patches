package app.revanced.patches.youtube.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.utils.MethodFingerprintUtils.resolve
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility
import app.revanced.patches.youtube.ad.video.fingerprints.ShowVideoAdsConstructorFingerprint
import app.revanced.patches.youtube.ad.video.fingerprints.ShowVideoAdsFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch

@Patch
@DependsOn([IntegrationsPatch::class])
@Name("video-ads")
@Description("Removes ads in the video player.")
@VideoAdsCompatibility
@Version("0.0.1")
class VideoAdsPatch : BytecodePatch(
    listOf(
        ShowVideoAdsConstructorFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        ShowVideoAdsFingerprint.resolve(data, ShowVideoAdsConstructorFingerprint.result!!.classDef)

        // Override the parameter by calling shouldShowAds and setting the parameter to the result
        ShowVideoAdsFingerprint.result!!.mutableMethod.addInstructions(
            0, """
                invoke-static { }, Lapp/revanced/integrations/patches/VideoAdsPatch;->shouldShowAds()Z
                move-result v1
            """
        )

        return PatchResultSuccess()
    }
}
