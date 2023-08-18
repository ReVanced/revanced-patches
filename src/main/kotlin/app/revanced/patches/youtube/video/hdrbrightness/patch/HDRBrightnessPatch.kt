package app.revanced.patches.youtube.video.hdrbrightness.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.video.hdrbrightness.annotations.HDRBrightnessCompatibility
import app.revanced.patches.youtube.video.hdrbrightness.fingerprints.HDRBrightnessFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch
@Name("HDR auto brightness")
@Description("Makes the brightness of HDR videos follow the system default.")
@HDRBrightnessCompatibility
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
class HDRBrightnessPatch : BytecodePatch(
    listOf(HDRBrightnessFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference(
                "revanced_hdr_auto_brightness",
                StringResource("revanced_hdr_auto_brightness_title", "Enable auto HDR brightness"),
                StringResource("revanced_hdr_auto_brightness_summary_on", "Auto HDR brightness is enabled"),
                StringResource("revanced_hdr_auto_brightness_summary_off", "Auto HDR brightness is disabled")
            )
        )

        val method = HDRBrightnessFingerprint.result!!.mutableMethod

        method.implementation!!.instructions.filter { instruction ->
            val fieldReference = (instruction as? ReferenceInstruction)?.reference as? FieldReference
            fieldReference?.let { it.name == "screenBrightness" } == true
        }.forEach { instruction ->
            val brightnessRegisterIndex = method.implementation!!.instructions.indexOf(instruction)
            val register = (instruction as TwoRegisterInstruction).registerA

            val insertIndex = brightnessRegisterIndex + 1
            method.addInstructions(
                insertIndex,
                """
                   invoke-static {v$register}, Lapp/revanced/integrations/patches/HDRAutoBrightnessPatch;->getHDRBrightness(F)F
                   move-result v$register
                """
            )
        }
    }
}
