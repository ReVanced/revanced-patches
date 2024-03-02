package app.revanced.patches.twitch.ad.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.ad.shared.util.BaseAdPatch
import app.revanced.patches.twitch.ad.video.fingerprints.CheckAdEligibilityLambdaFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.ContentConfigShowAdsFingerprint
import app.revanced.patches.twitch.ad.video.fingerprints.GetReadyToShowAdFingerprint
import app.revanced.patches.twitch.misc.integrations.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Block video ads",
    description = "Blocks video ads in streams and VODs.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [CompatiblePackage("tv.twitch.android.app", ["15.4.1", "16.1.0", "16.9.1"])]
)
object VideoAdsPatch : BaseAdPatch(
    "Lapp/revanced/integrations/twitch/patches/VideoAdsPatch;->shouldBlockVideoAds()Z",
    "show_video_ads",
    setOf(
        ContentConfigShowAdsFingerprint,
        CheckAdEligibilityLambdaFingerprint,
        GetReadyToShowAdFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference("revanced_block_video_ads")
        )

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
    }
}
