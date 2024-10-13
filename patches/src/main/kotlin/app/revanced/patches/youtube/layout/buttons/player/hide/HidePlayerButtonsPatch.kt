package app.revanced.patches.youtube.layout.buttons.player.hide

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction3rc

@Suppress("unused")
val hidePlayerButtonsPatch = bytecodePatch(
    name = "Hide player buttons",
    description = "Adds an option to hide the previous and next buttons in the video player.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val playerControlsVisibilityModelMatch by playerControlsVisibilityModelFingerprint()

    val ParameterOffsets = object {
        val HAS_NEXT = 5
        val HAS_PREVIOUS = 6
    }

    execute {
        addResources("youtube", "layout.buttons.player.hide.hidePlayerButtonsPatch")

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_player_buttons"),
        )

        playerControlsVisibilityModelMatch.apply {
            val callIndex = patternMatch!!.endIndex
            val callInstruction = mutableMethod.getInstruction<Instruction3rc>(callIndex)

            // overriding this parameter register hides the previous and next buttons
            val hasNextParameterRegister = callInstruction.startRegister + ParameterOffsets.HAS_NEXT
            val hasPreviousParameterRegister = callInstruction.startRegister + ParameterOffsets.HAS_PREVIOUS

            mutableMethod.addInstructions(
                callIndex,
                """
                    invoke-static { v$hasNextParameterRegister }, Lapp/revanced/extension/youtube/patches/HidePlayerButtonsPatch;->previousOrNextButtonIsVisible(Z)Z
                    move-result v$hasNextParameterRegister
                    
                    invoke-static { v$hasPreviousParameterRegister }, Lapp/revanced/extension/youtube/patches/HidePlayerButtonsPatch;->previousOrNextButtonIsVisible(Z)Z
                    move-result v$hasPreviousParameterRegister
                """,
            )
        }
    }
}
