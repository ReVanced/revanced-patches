package app.revanced.patches.twitch.ad.video.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.twitch.ad.shared.util.AbstractAdPatch
import app.revanced.patches.twitch.ad.video.annotations.VideoAdsCompatibility
import app.revanced.patches.twitch.ad.video.fingerprints.CheckAdEligibilityLambdaFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.ContentConfigShowAdsFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.GetReadyToShowAdFingerprint
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("Block video ads")
@Description("Blocks video ads in streams and VODs.")
@VideoAdsCompatibility
class VideoAdsPatch : AbstractAdPatch(
    "Lapp/revanced/twitch/patches/VideoAdsPatch;->shouldBlockVideoAds()Z",
    "show_video_ads",
    listOf(
        ContentConfigShowAdsFingerprint,
        CheckAdEligibilityLambdaFingerprint,
        GetReadyToShowAdFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        /* Amazon ads SDK */
        context.blockMethods(
            "Lcom/amazon/ads/video/player/AdsManagerImpl;",
            "playAds"
        )

        /* Twitch ads manager */
        context.blockMethods(
            "Ltv/twitch/android/shared/ads/VideoAdManager;",
            "checkAdEligibilityAndRequestAd", "requestAd", "requestAds"
        )

        /* Various ad presenters */
        context.blockMethods(
            "Ltv/twitch/android/shared/ads/AdsPlayerPresenter;",
            "requestAd", "requestFirstAd", "requestFirstAdIfEligible", "requestMidroll", "requestAdFromMultiAdFormatEvent"
        )

        context.blockMethods(
            "Ltv/twitch/android/shared/ads/AdsVodPlayerPresenter;",
            "requestAd", "requestFirstAd",
        )

        context.blockMethods(
            "Ltv/twitch/android/feature/theatre/ads/AdEdgeAllocationPresenter;",
            "parseAdAndCheckEligibility", "requestAdsAfterEligibilityCheck", "showAd", "bindMultiAdFormatAllocation"
        )

        /* A/B ad testing experiments */
        context.blockMethods(
            "Ltv/twitch/android/provider/experiments/helpers/DisplayAdsExperimentHelper;",
            "areDisplayAdsEnabled",
            returnMethod = ReturnMethod('Z', "0")
        )

        context.blockMethods(
            "Ltv/twitch/android/shared/ads/tracking/MultiFormatAdsTrackingExperiment;",
            "shouldUseMultiAdFormatTracker", "shouldUseVideoAdTracker",
            returnMethod = ReturnMethod('Z', "0")
        )

        context.blockMethods(
            "Ltv/twitch/android/shared/ads/MultiformatAdsExperiment;",
            "shouldDisableClientSideLivePreroll", "shouldDisableClientSideVodPreroll",
            returnMethod = ReturnMethod('Z', "1")
        )

        // Pretend our player is ineligible for all ads
        CheckAdEligibilityLambdaFingerprint.result?.apply {
            mutableMethod.addInstructionsWithLabels(
                0,
                """
                    ${createConditionInstructions()}
                    const/4 v0, 0 
                    invoke-static {v0}, Lio/reactivex/Single;->just(Ljava/lang/Object;)Lio/reactivex/Single;
                    move-result-object p0
                    return-object p0
                """,
                ExternalLabel(skipLabelName, mutableMethod.getInstruction(0))
            )
        } ?: throw CheckAdEligibilityLambdaFingerprint.exception

        GetReadyToShowAdFingerprint.result?.apply {
            val adFormatDeclined = "Ltv/twitch/android/shared/display/ads/theatre/StreamDisplayAdsPresenter\$Action\$AdFormatDeclined;"
            mutableMethod.addInstructionsWithLabels(
                0,
                """
                    ${createConditionInstructions()}
                    sget-object p2, $adFormatDeclined->INSTANCE:$adFormatDeclined
                    invoke-static {p1, p2}, Ltv/twitch/android/core/mvp/presenter/StateMachineKt;->plus(Ltv/twitch/android/core/mvp/presenter/PresenterState;Ltv/twitch/android/core/mvp/presenter/PresenterAction;)Ltv/twitch/android/core/mvp/presenter/StateAndAction;
                    move-result-object p1
                    return-object p1
                """,
                ExternalLabel(skipLabelName, mutableMethod.getInstruction(0))
            )
        } ?: throw GetReadyToShowAdFingerprint.exception

        // Spoof showAds JSON field
        ContentConfigShowAdsFingerprint.result?.apply {
            mutableMethod.addInstructions(0, """
                    ${createConditionInstructions()}
                    const/4 v0, 0
                    :$skipLabelName
                    return v0
                """
            )
        }  ?: throw ContentConfigShowAdsFingerprint.exception

        SettingsPatch.PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference(
                "revanced_block_video_ads",
                StringResource(
                    "revanced_block_video_ads",
                    "Block video ads"
                ),
                StringResource(
                    "revanced_block_video_ads_on",
                    "Video ads are blocked"
                ),
                StringResource(
                    "revanced_block_video_ads_off",
                    "Video ads are unblocked"
                ),
                default = true
            )
        )
    }
}
