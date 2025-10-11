package app.revanced.patches.youtube.video.quality

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playercontrols.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

private val videoQualityButtonResourcePatch = resourcePatch {
    dependsOn(playerControlsPatch)

    execute {
        copyResources(
            "qualitybutton",
            ResourceGroup(
                "drawable",
                "revanced_video_quality_dialog_button_rectangle.xml",
            ),
        )

        addBottomControl("qualitybutton")
    }
}

private const val QUALITY_BUTTON_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/videoplayer/VideoQualityDialogButton;"

val videoQualityDialogButtonPatch = bytecodePatch(
    description = "Adds the option to display video quality dialog button in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        rememberVideoQualityPatch,
        videoQualityButtonResourcePatch,
        playerControlsPatch,
    )

    execute {
        addResources("youtube", "video.quality.button.videoQualityDialogButtonPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_video_quality_dialog_button"),
        )

        initializeBottomControl(QUALITY_BUTTON_CLASS_DESCRIPTOR)
        injectVisibilityCheckCall(QUALITY_BUTTON_CLASS_DESCRIPTOR)
    }
}
