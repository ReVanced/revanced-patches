package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.doubleSpeedSeekNoticeFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.slideToSeekFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val enableSlideToSeekPatch = bytecodePatch(
    name = "Enable slide to seek",
    description = "Adds an option to enable slide to seek instead of playing at 2x speed when pressing and holding in the video player. Including this patch may cause issues with tapping or double tapping the video player overlay.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch
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
        ),
    )

    val slideToSeekResult by slideToSeekFingerprint
    val doubleSpeedSeekNoticeResult by doubleSpeedSeekNoticeFingerprint

    val integrationsClassDescriptor = "Lapp/revanced/integrations/youtube/patches/SlideToSeekPatch;"

    execute {
        addResources(this)

        PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek"),
        )

        arrayOf(
            // Restore the behaviour to slide to seek.
            slideToSeekResult,
            // Disable the double speed seek notice.
            doubleSpeedSeekNoticeResult,
        ).forEach {
            val insertIndex = it.scanResult.patternScanResult!!.endIndex + 1

            it.mutableMethod.apply {
                val isEnabledRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static { }, $integrationsClassDescriptor->isSlideToSeekDisabled()Z
                        move-result v$isEnabledRegister
                    """,
                )
            }
        }
    }
}
