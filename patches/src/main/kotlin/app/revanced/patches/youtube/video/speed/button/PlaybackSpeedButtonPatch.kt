package app.revanced.patches.youtube.video.speed.button

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.speed.custom.customPlaybackSpeedPatch
import app.revanced.util.ResourceGroup
import app.revanced.util.copyResources

private val playbackSpeedButtonResourcePatch = resourcePatch {
    dependsOn(playerControlsResourcePatch)

    execute {
        copyResources(
            "speedbutton",
            ResourceGroup(
                "drawable",
                "revanced_playback_speed_dialog_button.xml",
            ),
        )

        addBottomControl("speedbutton")
    }
}

private const val SPEED_BUTTON_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/videoplayer/PlaybackSpeedDialogButton;"

val playbackSpeedButtonPatch = bytecodePatch(
    description = "Adds the option to display playback speed dialog button in the video player.",
) {
    dependsOn(
        playbackSpeedButtonResourcePatch,
        customPlaybackSpeedPatch,
        playerControlsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "video.speed.button.playbackSpeedButtonPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_playback_speed_dialog_button"),
        )

        initializeBottomControl(SPEED_BUTTON_CLASS_DESCRIPTOR)
        injectVisibilityCheckCall(SPEED_BUTTON_CLASS_DESCRIPTOR)
    }
}
