package app.revanced.patches.twitch.ad.audio.patch

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
import app.revanced.patches.twitch.ad.audio.annotations.AudioAdsCompatibility
import app.revanced.patches.twitch.ad.audio.fingerprints.AudioAdsPresenterPlayFingerprint
import app.revanced.patches.twitch.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.twitch.misc.settings.bytecode.patch.SettingsPatch

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
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
            mutableMethod.addInstructions(
                0,
                """
                    invoke-static { }, Lapp/revanced/twitch/patches/AudioAdsPatch;->shouldBlockAudioAds()Z
                    move-result v0
                    if-eqz v0, :show_audio_ads
                    return-void
                """,
                listOf(ExternalLabel("show_audio_ads", mutableMethod.instruction(0)))
            )
        }

        SettingsPatch.PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference(
                "revanced_block_audio_ads",
                StringResource(
                    "revanced_block_audio_ads",
                    "Block audio ads"
                ),
                true,
                StringResource(
                    "revanced_block_audio_ads_on",
                    "Audio ads are blocked"
                ),
                StringResource(
                    "revanced_block_audio_ads_off",
                    "Audio ads are unblocked"
                ),
            )
        )

        return PatchResultSuccess()
    }
}
