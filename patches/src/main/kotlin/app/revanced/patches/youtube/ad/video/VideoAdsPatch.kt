package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

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
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        )
    )

    execute {
        addResources("youtube", "ad.video.videoAdsPatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_video_ads"),
        )

        loadVideoAdsFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/extension/youtube/patches/VideoAdsPatch;->shouldShowAds()Z
                move-result v0
                if-nez v0, :show_video_ads
                return-void
            """,
            ExternalLabel("show_video_ads", loadVideoAdsFingerprint.method.getInstruction(0)),
        )
    }
}
