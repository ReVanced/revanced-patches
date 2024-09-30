package app.revanced.patches.youtube.video.speed.button

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.playercontrols.PlayerControlsBytecodePatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.speed.custom.CustomPlaybackSpeedPatch

@Patch(
    description = "Adds the option to display playback speed dialog button in the video player.",
    dependencies = [
        PlaybackSpeedButtonResourcePatch::class,
        CustomPlaybackSpeedPatch::class,
        PlayerControlsBytecodePatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
    ],
)
@Suppress("unused")
object PlaybackSpeedButtonPatch : BytecodePatch(emptySet()) {
    private const val SPEED_BUTTON_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/videoplayer/PlaybackSpeedDialogButton;"

    override fun execute(context: BytecodeContext) {

        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_playback_speed_dialog_button"),
        )

        PlayerControlsBytecodePatch.initializeBottomControl(SPEED_BUTTON_CLASS_DESCRIPTOR)
        PlayerControlsBytecodePatch.injectVisibilityCheckCall(SPEED_BUTTON_CLASS_DESCRIPTOR)
    }
}