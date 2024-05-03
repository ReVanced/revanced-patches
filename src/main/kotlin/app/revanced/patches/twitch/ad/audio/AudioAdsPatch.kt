package app.revanced.patches.twitch.ad.audio

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.ad.audio.fingerprints.audioAdsPresenterPlayFingerprint
import app.revanced.patches.twitch.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.integrations.integrationsPatch

@Suppress("unused")
val audioAdsPatch = bytecodePatch(
    name = "Block audio ads",
    description = "Blocks audio ads in streams and VODs.",
) {
    dependsOn(integrationsPatch, SettingsPatch, addResourcesPatch)

    compatibleWith("tv.twitch.android.app"("15.4.1", "16.1.0", "16.9.1"))

    val audioAdsPresenterPlayResult by audioAdsPresenterPlayFingerprint

    execute {
        addResources(this)

        SettingsPatch.PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference("revanced_block_audio_ads"),
        )

        // Block playAds call
        with(audioAdsPresenterPlayResult) {
            mutableMethod.addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, Lapp/revanced/integrations/twitch/patches/AudioAdsPatch;->shouldBlockAudioAds()Z
                    move-result v0
                    if-eqz v0, :show_audio_ads
                    return-void
                """,
                ExternalLabel("show_audio_ads", mutableMethod.getInstruction(0)),
            )
        }
    }
}
