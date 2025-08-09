package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.seekbarColorPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_20_28_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.seekbarFingerprint
import app.revanced.patches.youtube.shared.seekbarOnDrawFingerprint
import app.revanced.util.insertLiteralOverride

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/HideSeekbarPatch;"

val hideSeekbarPatch = bytecodePatch(
    description = "Adds an option to hide the seekbar.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        seekbarColorPatch,
        addResourcesPatch,
        versionCheckPatch
    )

    execute {
        addResources("youtube", "layout.hide.seekbar.hideSeekbarPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_hide_seekbar"),
            SwitchPreference("revanced_hide_seekbar_thumbnail"),
            SwitchPreference("revanced_fullscreen_large_seekbar"),
        )

        seekbarOnDrawFingerprint.match(seekbarFingerprint.originalClassDef).method.addInstructionsWithLabels(
            0,
            """
                const/4 v0, 0x0
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->hideSeekbar()Z
                move-result v0
                if-eqz v0, :hide_seekbar
                return-void
                :hide_seekbar
                nop
            """
        )

        if (is_20_28_or_greater) {
            fullscreenLargeSeekbarFeatureFlagFingerprint.let {
                it.method.insertLiteralOverride(
                    it.instructionMatches.first().index,
                    "$EXTENSION_CLASS_DESCRIPTOR->useFullscreenLargeSeekbar(Z)Z"
                )
            }
        }
    }
}
