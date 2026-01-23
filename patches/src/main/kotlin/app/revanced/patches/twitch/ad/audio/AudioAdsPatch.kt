package app.revanced.patches.twitch.ad.audio

import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.twitch.misc.extension.sharedExtensionPatch
import app.revanced.patches.twitch.misc.settings.PreferenceScreen
import app.revanced.patches.twitch.misc.settings.Settings

val `Block audio ads` by creatingBytecodePatch(
    description = "Blocks audio ads in streams and VODs.",
) {
    dependsOn(
        sharedExtensionPatch,
        Settings,
        addResourcesPatch,
    )

    compatibleWith("tv.twitch.android.app"("16.9.1", "25.3.0"))

    apply {
        addResources("twitch", "ad.audio.audioAdsPatch")

        PreferenceScreen.ADS.CLIENT_SIDE.addPreferences(
            SwitchPreference("revanced_block_audio_ads"),
        )

        // Block playAds call
        audioAdsPresenterPlayMethod.addInstructionsWithLabels(
            0,
            """
                    invoke-static { }, Lapp/revanced/extension/twitch/patches/AudioAdsPatch;->shouldBlockAudioAds()Z
                    move-result v0
                    if-eqz v0, :show_audio_ads
                    return-void
                """,
            ExternalLabel("show_audio_ads", audioAdsPresenterPlayMethod.getInstruction(0)),
        )
    }
}
