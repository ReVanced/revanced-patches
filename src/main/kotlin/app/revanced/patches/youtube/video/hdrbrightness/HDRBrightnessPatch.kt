package app.revanced.patches.youtube.video.hdrbrightness

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.hdrbrightness.fingerprints.HDRBrightnessFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Deprecated("Patch is obsolete and the hooked code is no longer present in 19.09+")
@Patch(
    description = "Adds an option to make the brightness of HDR videos follow the system default.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                // 19.09+ is dramatically different and the patched code is not present.
            ]
        )
    ]
)
@Suppress("unused")
object HDRBrightnessPatch : BytecodePatch(
    setOf(HDRBrightnessFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        if (HDRBrightnessFingerprint.result == null) throw HDRBrightnessFingerprint.exception

        SettingsPatch.PreferenceScreen.VIDEO.addPreferences(
            SwitchPreference("revanced_hdr_auto_brightness"),
        )

        // FIXME
        // One of the changes made here effectively does nothing:
        // It calls getHDRBrightness() and ignores the results.
        HDRBrightnessFingerprint.result?.mutableMethod?.apply {
            implementation!!.instructions.filter { instruction ->
                ((instruction as? ReferenceInstruction)?.reference as? FieldReference)
                    ?.name == "screenBrightness"
            }.forEach { instruction ->
                val brightnessRegisterIndex = implementation!!.instructions.indexOf(instruction)
                val register = (instruction as TwoRegisterInstruction).registerA
                val insertIndex = brightnessRegisterIndex + 1
                addInstructions(
                    insertIndex,
                    """
                        invoke-static {v$register}, Lapp/revanced/integrations/youtube/patches/HDRAutoBrightnessPatch;->getHDRBrightness(F)F
                        move-result v$register
                    """,
                )
            }
        } ?: throw HDRBrightnessFingerprint.exception
    }
}
