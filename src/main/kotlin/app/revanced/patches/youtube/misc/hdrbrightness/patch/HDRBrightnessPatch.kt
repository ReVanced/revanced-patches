package app.revanced.patches.youtube.misc.hdrbrightness.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.hdrbrightness.annotations.HDRBrightnessCompatibility
import app.revanced.patches.youtube.misc.hdrbrightness.fingerprints.HDRBrightnessFingerprintXXZ
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.settings.framework.components.impl.StringResource
import app.revanced.patches.youtube.misc.settings.framework.components.impl.SwitchPreference
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@Name("hdr-auto-brightness")
@Description("Makes the brightness of HDR videos follow the system default.")
@HDRBrightnessCompatibility
@Version("0.0.2")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
class HDRBrightnessPatch : BytecodePatch(
    listOf(
        HDRBrightnessFingerprintXXZ
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_pref_hdr_autobrightness",
                StringResource("revanced_hdr_autobrightness_enabled_title", "Enable auto HDR brightness"),
                true,
                StringResource("revanced_hdr_autobrightness_summary_on", "Auto HDR brightness is enabled"),
                StringResource("revanced_hdr_autobrightness_summary_off", "Auto HDR brightness is disabled")
            )
        )

        val method = HDRBrightnessFingerprintXXZ.result?.mutableMethod
            ?: return PatchResultError("HDRBrightnessFingerprint could not resolve the method!")

        method.implementation!!.instructions.filter {
            ((it as? ReferenceInstruction)?.reference as? FieldReference)?.let { field ->
                // iput vx, vy, Landroid/view/WindowManager$LayoutParams;->screenBrightness:F
                field.definingClass == "Landroid/view/WindowManager\$LayoutParams;" && field.name == "screenBrightness"
            } == true
        }.forEach { instruction ->
            // inject right before the call that sets 'screenBrightness'
            val index = method.implementation!!.instructions.indexOf(instruction)
            val register = (instruction as TwoRegisterInstruction).registerA

            // inject the call to
            method.addInstructions(
                index, """
           invoke-static {v$register}, Lapp/revanced/integrations/patches/HDRAutoBrightnessPatch;->getHDRBrightness(F)F
           move-result v$register
        """
            )
        }
        return PatchResultSuccess()
    }
}
