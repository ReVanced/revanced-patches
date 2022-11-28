package app.revanced.patches.twitch.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.twitch.ad.video.annotations.VideoAdsCompatibility
import app.revanced.patches.twitch.ad.video.fingerprints.AdsManagerFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.CheckAdEligibilityLambdaFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.ContentConfigShowAdsFingerprint
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
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
    private fun createConditionInstructions(register: String = "v0") = """
        invoke-static { }, Lapp/revanced/twitch/patches/VideoAdsPatch;->shouldBlockVideoAds()Z
        move-result $register
        if-eqz $register, :show_video_ads
    """

    override fun execute(context: BytecodeContext): PatchResult {
        // Pretend our player is ineligible for all ads
        with(CheckAdEligibilityLambdaFingerprint.result!!) {
            mutableMethod.addInstructions(
                0,
                """
                    ${createConditionInstructions()}
                    const/4 v0, 0 
                    invoke-static {v0}, Lio/reactivex/Single;->just(Ljava/lang/Object;)Lio/reactivex/Single;
                    move-result-object p0
                    return-object p0
                """,
                listOf(ExternalLabel("show_video_ads", mutableMethod.instruction(0)))
            )
        }

        // Spoof showAds JSON field
        with(ContentConfigShowAdsFingerprint.result!!) {
            mutableMethod.addInstructions(0, """
                    ${createConditionInstructions()}
                    const/4 v0, 0
                    :show_video_ads
                    return v0
                """
            )
        }

        // Block playAds call
        with(AdsManagerFingerprint.result!!) {
            mutableMethod.addInstructions(
                0,
                """
                    ${createConditionInstructions()}
                    return-void
                """,
                listOf(ExternalLabel("show_video_ads", mutableMethod.instruction(0)))
            )
        }

        SettingsPatch.PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference(
                "revanced_block_video_ads",
                StringResource(
                    "revanced_block_video_ads",
                    "Block video ads"
                ),
                true,
                StringResource(
                    "revanced_block_video_ads_on",
                    "Video ads are blocked"
                ),
                StringResource(
                    "revanced_block_video_ads_off",
                    "Video ads are unblocked"
                ),
            )
        )

        return PatchResultSuccess()
    }
}
