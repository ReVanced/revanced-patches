package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.ad.video.fingerprints.LoadVideoAdsFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Video ads",
    description = "Adds an option to remove ads in the video player.",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ]
)
@Suppress("unused")
object VideoAdsPatch : BytecodePatch(
    setOf(LoadVideoAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.ADS.addPreferences(
            SwitchPreference(
                "revanced_hide_video_ads",
                StringResource("revanced_hide_video_ads_title", "Hide video ads"),
                StringResource("revanced_hide_video_ads_summary_on", "Video ads are hidden"),
                StringResource("revanced_hide_video_ads_summary_off", "Video ads are shown")
            )
        )

        val loadVideoAdsFingerprintMethod = LoadVideoAdsFingerprint.result!!.mutableMethod

        loadVideoAdsFingerprintMethod.addInstructionsWithLabels(
            0, """
                invoke-static { }, Lapp/revanced/integrations/youtube/patches/VideoAdsPatch;->shouldShowAds()Z
                move-result v0
                if-nez v0, :show_video_ads
                return-void
            """,
            ExternalLabel("show_video_ads", loadVideoAdsFingerprintMethod.getInstruction(0))
        )
    }
}
