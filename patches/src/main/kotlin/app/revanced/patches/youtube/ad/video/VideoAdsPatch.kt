package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.contexthook.Endpoint
import app.revanced.patches.youtube.misc.contexthook.addOSNameHook
import app.revanced.patches.youtube.misc.contexthook.hookClientContextPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/VideoAdsPatch;"

@Suppress("ObjectPropertyName")
val videoAdsPatch = bytecodePatch(
    name = "Video ads",
    description = "Adds an option to remove ads in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        hookClientContextPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45",
            "20.44.38"
        ),
    )

    apply {
        addResources("youtube", "ad.video.videoAdsPatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_video_ads"),
        )

        setOf(
            loadVideoAdsMethod,
            playerBytesAdLayoutMethod,
        ).forEach { method ->
            method.addInstructionsWithLabels(
                0,
                """
                    invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->hideVideoAds()Z
                    move-result v0
                    if-eqz v0, :show_video_ads
                    return-void
                    :show_video_ads
                    nop
                """
            )
        }


        setOf(
            Endpoint.GET_WATCH,
            Endpoint.PLAYER,
            Endpoint.REEL,
        ).forEach { endpoint ->
            addOSNameHook(
                endpoint,
                "$EXTENSION_CLASS_DESCRIPTOR->hideVideoAds(Ljava/lang/String;)Ljava/lang/String;",
            )
        }
    }
}
