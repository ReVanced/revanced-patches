package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

val zoomHapticsPatch = bytecodePatch(
    name = "Disable zoom haptics",
    description = "Adds an option to disable haptics when zooming.",
) {
    dependsOn(
        settingsPatch,
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
        addResources("youtube", "misc.zoomhaptics.zoomHapticsPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_disable_zoom_haptics"),
        )

        zoomHapticsFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                invoke-static { }, Lapp/revanced/extension/youtube/patches/ZoomHapticsPatch;->shouldVibrate()Z
                move-result v0
                if-nez v0, :vibrate
                return-void
            """,
                ExternalLabel("vibrate", getInstruction(0)),
            )
        }
    }
}
