package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val videoAdsPatch = bytecodePatch(
    name = "Video ads",
    description = "Adds an option to remove ads in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val loadVideoAdsMatch by loadVideoAdsFingerprint()

    execute {
        addResources("youtube", "ad.video.videoAdsPatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_video_ads"),
        )

        loadVideoAdsMatch.mutableMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/extension/youtube/patches/VideoAdsPatch;->shouldShowAds()Z
                move-result v0
                if-nez v0, :show_video_ads
                return-void
            """,
            ExternalLabel("show_video_ads", loadVideoAdsMatch.mutableMethod.getInstruction(0)),
        )
    }
}
