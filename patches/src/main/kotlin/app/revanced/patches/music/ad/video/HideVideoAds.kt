package app.revanced.patches.music.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/music/patches/HideVideoAdsPatch;"

@Suppress("unused")
val hideVideoAdsPatch = bytecodePatch(
    name = "Hide music video ads",
    description = "Adds an option to hide ads that appear while listening to or streaming music videos, podcasts, or songs.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52"
        )
    )

    execute {
        addResources("music", "ad.video.hideVideoAdsPatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_music_hide_video_ads"),
        )

        navigate(showVideoAdsParentFingerprint.originalMethod)
            .to(showVideoAdsParentFingerprint.patternMatch!!.startIndex + 1)
            .stop()
            .addInstructions(
                0,
                """
                    invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->showVideoAds(Z)Z
                    move-result p1
                """
            )
    }
}
