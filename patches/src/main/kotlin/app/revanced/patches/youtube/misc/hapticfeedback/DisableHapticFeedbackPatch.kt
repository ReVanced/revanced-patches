package app.revanced.patches.youtube.misc.hapticfeedback

import app.revanced.patcher.extensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.extensions.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/DisableHapticFeedbackPatch;"

@Suppress("unused")
val disableHapticFeedbackPatch = bytecodePatch(
    name = "Disable haptic feedback",
    description = "Adds an option to disable haptic feedback in the player for various actions.",
) {
    dependsOn(
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
        addResources("youtube", "misc.hapticfeedback.disableHapticFeedbackPatch")

        PreferenceScreen.PLAYER.addPreferences(
            PreferenceScreenPreference(
                "revanced_disable_haptic_feedback",
                preferences = setOf(
                    SwitchPreference("revanced_disable_haptic_feedback_chapters"),
                    SwitchPreference("revanced_disable_haptic_feedback_precise_seeking"),
                    SwitchPreference("revanced_disable_haptic_feedback_seek_undo"),
                    SwitchPreference("revanced_disable_haptic_feedback_zoom"),
                )
            )
        )

        arrayOf(
            markerHapticsFingerprint to "disableChapterVibrate",
            scrubbingHapticsFingerprint to "disablePreciseSeekingVibrate",
            seekUndoHapticsFingerprint to "disableSeekUndoVibrate",
            zoomHapticsFingerprint to "disableZoomVibrate"
        ).forEach { (fingerprint, methodName) ->
            fingerprint.method.apply {
                addInstructionsWithLabels(
                    0,
                    """
                        invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->$methodName()Z
                        move-result v0
                        if-eqz v0, :vibrate
                        return-void
                    """,
                    ExternalLabel("vibrate", getInstruction(0))
                )
            }
        }
    }
}
