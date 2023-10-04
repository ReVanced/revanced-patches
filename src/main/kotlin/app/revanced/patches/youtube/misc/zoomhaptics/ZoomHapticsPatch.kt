package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.misc.zoomhaptics.fingerprints.ZoomHapticsFingerprint

@Patch(
    name = "Disable zoom haptics",
    description = "Disables haptics when zooming.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.google.android.youtube")]
)
@Suppress("unused")
object ZoomHapticsPatch : BytecodePatch(
    setOf(ZoomHapticsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_disable_zoom_haptics",
                StringResource("revanced_disable_zoom_haptics_title", "Disable zoom haptics"),
                StringResource("revanced_disable_zoom_haptics_summary_on", "Haptics are disabled"),
                StringResource("revanced_disable_zoom_haptics_summary_off", "Haptics are enabled")
            )
        )

        val zoomHapticsFingerprintMethod = ZoomHapticsFingerprint.result!!.mutableMethod

        zoomHapticsFingerprintMethod.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, Lapp/revanced/integrations/patches/ZoomHapticsPatch;->shouldVibrate()Z
                move-result v0
                if-nez v0, :vibrate
                return-void
            """,
            ExternalLabel("vibrate", zoomHapticsFingerprintMethod.getInstruction(0))
        )
    }
}