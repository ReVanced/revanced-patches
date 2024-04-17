package app.revanced.patches.youtube.interaction.seekbar

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.DoubleSpeedSeekNoticeFingerprint
import app.revanced.patches.youtube.interaction.seekbar.fingerprints.SlideToSeekFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable slide to seek",
    description = "Adds an option to enable slide to seek instead of playing at 2x speed when pressing and holding in the video player. Including this patch may cause issues with tapping or double tapping the video player overlay.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "19.11.43"
            ]
        )
    ],
    use = false
)
@Suppress("unused")
object EnableSlideToSeekPatch : BytecodePatch(
    setOf(
        SlideToSeekFingerprint,
        DoubleSpeedSeekNoticeFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/SlideToSeekPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.SEEKBAR.addPreferences(
            SwitchPreference("revanced_slide_to_seek")
        )

        arrayOf(
            // Restore the behaviour to slide to seek.
            SlideToSeekFingerprint,
            // Disable the double speed seek notice.
            DoubleSpeedSeekNoticeFingerprint
        ).map {
            it.result ?: throw it.exception
        }.forEach {
            val insertIndex = it.scanResult.patternScanResult!!.endIndex + 1

            it.mutableMethod.apply {
                val isEnabledRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->isSlideToSeekDisabled()Z
                        move-result v$isEnabledRegister
                    """
                )
            }
        }
    }
}