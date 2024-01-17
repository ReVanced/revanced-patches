package app.revanced.patches.youtube.video.hdrbrightness

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.hdrbrightness.fingerprints.HDRBrightnessFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Patch(
    name = "HDR auto brightness",
    description = "Adds an option to make the brightness of HDR videos follow the system default.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.34"
            ]
        )
    ]
)
@Suppress("unused")
object HDRBrightnessPatch : BytecodePatch(
    setOf(HDRBrightnessFingerprint)
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
                   invoke-static {v$register}, Lapp/revanced/integrations/youtube/patches/HDRAutoBrightnessPatch;->getHDRBrightness(F)F
                   move-result v$register
                """
            )
        }
    }
}
