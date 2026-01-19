package app.revanced.patches.youtube.misc.audiofocus

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/PauseOnAudioInterruptPatch;"

val pauseOnAudioInterruptPatch = bytecodePatch(
    name = "Pause on audio interrupt",
    description = "Adds an option to pause playback instead of lowering volume when other audio plays.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "misc.audiofocus.pauseOnAudioInterruptPatch")

        PreferenceScreen.MISC.addPreferences(
            SwitchPreference("revanced_pause_on_audio_interrupt"),
        )

        // Hook the builder method that creates AudioFocusRequest.
        // At the start, set the willPauseWhenDucked field (b) to true if setting is enabled.
        val builderMethod = audioFocusRequestBuilderFingerprint.method
        val builderClass = builderMethod.definingClass

        builderMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->shouldPauseOnAudioInterrupt()Z
                move-result v0
                if-eqz v0, :skip_override
                const/4 v0, 0x1
                iput-boolean v0, p0, $builderClass->b:Z
            """,
            ExternalLabel("skip_override", builderMethod.getInstruction(0)),
        )

        // Also hook the audio focus change listener as a backup.
        audioFocusChangeListenerFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->overrideAudioFocusChange(I)I
                move-result p1
            """
        )
    }
}
