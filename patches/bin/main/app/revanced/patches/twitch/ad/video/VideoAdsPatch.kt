package app.revanced.patches.twitch.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.ad.shared.util.ReturnMethod
import app.revanced.patches.twitch.ad.shared.util.adPatch
import app.revanced.patches.twitch.misc.extension.sharedExtensionPatch
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.settingsPatch

val videoAdsPatch = bytecodePatch(
    name = "Block video ads",
    description = "Blocks video ads in streams and VODs.",
) {
    val conditionCall = "Lapp/revanced/extension/twitch/patches/VideoAdsPatch;->shouldBlockVideoAds()Z"
    val skipLabelName = "show_video_ads"

    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        adPatch(conditionCall, skipLabelName) { createConditionInstructions, blockMethods ->

            execute {
                addResources("twitch", "ad.video.videoAdsPatch")

                PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
                    SwitchPreference("revanced_block_video_ads"),
                )

                /* Amazon ads SDK */
                blockMethods(
                    "Lcom/amazon/ads/video/player/AdsManagerImpl;",
                    setOf("playAds"),
                    ReturnMethod.default,
                )

                /* Twitch ads manager */
                blockMethods(
                    "Ltv/twitch/android/shared/ads/VideoAdManager;",
                    setOf(
                        "checkAdEligibilityAndRequestAd",
                        "requestAd",
                        "requestAds",
                    ),
                    ReturnMethod.default,
                )

                /* Various ad presenters */
                blockMethods(
                    "Ltv/twitch/android/shared/ads/AdsPlayerPresenter;",
                    setOf(
                        "requestAd",
                        "requestFirstAd",
                        "requestFirstAdIfEligible",
                        "requestMidroll",
                        "requestAdFromMultiAdFormatEvent",
                    ),
                    ReturnMethod.default,
                )

                blockMethods(
                    "Ltv/twitch/android/shared/ads/AdsVodPlayerPresenter;",
                    setOf(
                        "requestAd",
                        "requestFirstAd",
                    ),
                    ReturnMethod.default,
                )

                blockMethods(
                    "Ltv/twitch/android/feature/theatre/ads/AdEdgeAllocationPresenter;",
                    setOf(
                        "parseAdAndCheckEligibility",
                        "requestAdsAfterEligibilityCheck",
                        "showAd",
                        "bindMultiAdFormatAllocation",
                    ),
                    ReturnMethod.default,
                )

                /* A/B ad testing experiments */
                blockMethods(
                    "Ltv/twitch/android/provider/experiments/helpers/DisplayAdsExperimentHelper;",
                    setOf("areDisplayAdsEnabled"),
                    ReturnMethod('Z', "0"),
                )

                blockMethods(
                    "Ltv/twitch/android/shared/ads/tracking/MultiFormatAdsTrackingExperiment;",
                    setOf(
                        "shouldUseMultiAdFormatTracker",
                        "shouldUseVideoAdTracker",
                    ),
                    ReturnMethod('Z', "0"),
                )

                blockMethods(
                    "Ltv/twitch/android/shared/ads/MultiformatAdsExperiment;",
                    setOf(
                        "shouldDisableClientSideLivePreroll",
                        "shouldDisableClientSideVodPreroll",
                    ),
                    ReturnMethod('Z', "1"),
                )

                // Pretend our player is ineligible for all ads.
                checkAdEligibilityLambdaFingerprint.method.addInstructionsWithLabels(
                    0,
                    """
                        ${createConditionInstructions("v0")}
                        const/4 v0, 0 
                        invoke-static {v0}, Lio/reactivex/Single;->just(Ljava/lang/Object;)Lio/reactivex/Single;
                        move-result-object p0
                        return-object p0
                    """,
                    ExternalLabel(
                        skipLabelName,
                        checkAdEligibilityLambdaFingerprint.method.getInstruction(0),
                    ),
                )

                val adFormatDeclined =
                    "Ltv/twitch/android/shared/display/ads/theatre/StreamDisplayAdsPresenter\$Action\$AdFormatDeclined;"
                getReadyToShowAdFingerprint.method.addInstructionsWithLabels(
                    0,
                    """
                    ${createConditionInstructions("v0")}
                    sget-object p2, $adFormatDeclined->INSTANCE:$adFormatDeclined
                    invoke-static {p1, p2}, Ltv/twitch/android/core/mvp/presenter/StateMachineKt;->plus(Ltv/twitch/android/core/mvp/presenter/PresenterState;Ltv/twitch/android/core/mvp/presenter/PresenterAction;)Ltv/twitch/android/core/mvp/presenter/StateAndAction;
                    move-result-object p1
                    return-object p1
                """,
                    ExternalLabel(skipLabelName, getReadyToShowAdFingerprint.method.getInstruction(0)),
                )

                // Spoof showAds JSON field.
                // Late versions of the app don't have the method anymore.
                contentConfigShowAdsFingerprint.methodOrNull?.addInstructions(
                    0,
                    """
                        ${createConditionInstructions("v0")}
                        const/4 v0, 0
                        :$skipLabelName
                        return v0
                    """,
                )
            }
        },
    )

    compatibleWith(
        "tv.twitch.android.app",
    )
}
