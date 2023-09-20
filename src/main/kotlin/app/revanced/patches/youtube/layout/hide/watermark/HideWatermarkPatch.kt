package app.revanced.patches.youtube.layout.hide.watermark

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.hide.watermark.fingerprints.HideWatermarkFingerprint
import app.revanced.patches.youtube.layout.hide.watermark.fingerprints.HideWatermarkParentFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch

@Patch(
    name = "Hide watermark",
    description = "Hides creator's watermarks on videos.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.16.37",
                "18.19.35",
                "18.20.39",
                "18.23.35",
                "18.29.38",
                "18.32.39"
            ]
        )
    ]
)
@Suppress("unused")
object HideWatermarkPatch : BytecodePatch(
    setOf(HideWatermarkParentFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_hide_video_watermark",
                StringResource("revanced_hide_video_watermark_title", "Hide creator watermark on videos"),
                StringResource("revanced_hide_video_watermark_summary_on", "Watermark is hidden"),
                StringResource("revanced_hide_video_watermark_summary_off", "Watermark is shown")
            )
        )

        HideWatermarkFingerprint.resolve(context, HideWatermarkParentFingerprint.result!!.classDef)
        val result = HideWatermarkFingerprint.result
            ?: throw PatchException("Required parent method could not be found.")

        val method = result.mutableMethod
        val line = method.implementation!!.instructions.size - 5

        method.removeInstruction(line)
        method.addInstructions(
            line,
            """
                invoke-static {}, Lapp/revanced/integrations/patches/BrandingWaterMarkPatch;->isBrandingWatermarkShown()Z
                move-result p2
            """
        )
    }
}
