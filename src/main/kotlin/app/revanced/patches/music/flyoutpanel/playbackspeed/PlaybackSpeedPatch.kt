package app.revanced.patches.music.flyoutpanel.playbackspeed

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.flyoutbutton.FlyoutButtonContainerPatch
import app.revanced.patches.music.utils.overridespeed.OverrideSpeedHookPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch

@Patch(
    name = "Enable playback speed",
    description = "Adds an option to add a playback speed button to the flyout panel.",
    dependencies = [
        FlyoutButtonContainerPatch::class,
        OverrideSpeedHookPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object PlaybackSpeedPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {

        SettingsPatch.addMusicPreference(
            CategoryType.FLYOUT,
            "revanced_enable_flyout_panel_playback_speed",
            "false"
        )

    }
}
