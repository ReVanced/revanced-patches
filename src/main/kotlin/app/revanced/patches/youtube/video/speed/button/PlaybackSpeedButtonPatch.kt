package app.revanced.patches.youtube.video.speed.button

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.initializeControl
import app.revanced.patches.youtube.misc.playercontrols.injectVisibilityCheckCall
import app.revanced.patches.youtube.misc.playercontrols.playerControlsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.video.speed.custom.customPlaybackSpeedPatch

private const val SPEED_BUTTON_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/videoplayer/PlaybackSpeedDialogButton;"

@Suppress("unused")
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
        addResources("youtube", "video.speed.button.PlaybackSpeedButtonPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_playback_speed_dialog_button"),
        )

        initializeControl("$SPEED_BUTTON_CLASS_DESCRIPTOR->initializeButton(Landroid/view/View;)V")
        injectVisibilityCheckCall("$SPEED_BUTTON_CLASS_DESCRIPTOR->changeVisibility(Z)V")
    }
}
