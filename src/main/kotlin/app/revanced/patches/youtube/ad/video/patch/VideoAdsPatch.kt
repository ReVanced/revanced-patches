package app.revanced.patches.youtube.ad.video.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.ad.video.annotations.VideoAdsCompatibility
import app.revanced.patches.youtube.ad.video.fingerprints.LoadVideoAdsFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("Video ads")
@Description("Removes ads in the video player.")
@VideoAdsCompatibility
class VideoAdsPatch : BytecodePatch(
    listOf(
        LoadVideoAdsFingerprint,
    )
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
                invoke-static { }, Lapp/revanced/integrations/patches/VideoAdsPatch;->shouldShowAds()Z
                move-result v0
                if-nez v0, :show_video_ads
                return-void
            """,
            ExternalLabel("show_video_ads", loadVideoAdsFingerprintMethod.getInstruction(0))
        )
    }
}
