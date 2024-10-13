package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/SlideToSeekPatch;"

@Suppress("unused")
val enableSlideToSeekPatch = bytecodePatch(
    name = "Enable slide to seek",
    description = "Adds an option to enable slide to seek instead of playing at 2x speed when pressing and holding in the video player. Including this patch may cause issues with tapping or double tapping the video player overlay.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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

    val slideToSeekMatch by slideToSeekFingerprint()
    val doubleSpeedSeekNoticeMatch by doubleSpeedSeekNoticeFingerprint()

    execute {
        addResources("youtube", "interaction.seekbar.enableSlideToSeekPatch")

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek"),
        )

        arrayOf(
            // Restore the behaviour to slide to seek.
            slideToSeekMatch,
            // Disable the double speed seek notice.
            doubleSpeedSeekNoticeMatch,
        ).forEach {
            val insertIndex = it.patternMatch!!.endIndex + 1

            it.mutableMethod.apply {
                val isEnabledRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->isSlideToSeekDisabled()Z
                        move-result v$isEnabledRegister
                    """,
                )
            }
        }
    }
}
