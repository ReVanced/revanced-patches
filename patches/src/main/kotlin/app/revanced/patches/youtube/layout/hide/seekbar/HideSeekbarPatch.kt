package app.revanced.patches.youtube.layout.hide.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.seekbar.seekbarColorPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.seekbarFingerprint
import app.revanced.patches.youtube.shared.seekbarOnDrawFingerprint

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/HideSeekbarPatch;"

val hideSeekbarPatch = bytecodePatch(
    name = "Hide seekbar",
    description = "Adds an option to hide the seekbar.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        seekbarColorPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
        ),
    )

    execute {
        addResources("youtube", "layout.hide.seekbar.hideSeekbarPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_hide_seekbar"),
            SwitchPreference("revanced_hide_seekbar_thumbnail"),
        )

        seekbarOnDrawFingerprint.match(seekbarFingerprint.classDef).method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->hideSeekbar()Z
                move-result v0
                if-eqz v0, :hide_seekbar
                return-void
                :hide_seekbar
                nop
            """,
        )
    }
}
