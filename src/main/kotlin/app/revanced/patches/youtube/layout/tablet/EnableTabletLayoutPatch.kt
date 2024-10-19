package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.tablet.fingerprints.GetFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.resultOrThrow

@Patch(
    name = "Enable tablet layout",
    description = "Adds an option to enable tablet layout",
    dependencies = [
        IntegrationsPatch::class,
        SettingsPatch::class,
        AddResourcesPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", arrayOf(
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            )
        )
    ]
)
@Suppress("unused")
object EnableTabletLayoutPatch : BytecodePatch(setOf(GetFormFactorFingerprint)) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/TabletLayoutPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_layout")
        )

        GetFormFactorFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val returnIsLargeFormFactorIndex = getInstructions().lastIndex - 4
                val returnIsLargeFormFactorLabel = getInstruction(returnIsLargeFormFactorIndex)

                addInstructionsWithLabels(
                    0,
                    """
                          invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->getTabletLayoutEnabled()Z
                          move-result v0
                          if-nez v0, :is_large_form_factor
                    """,
                    ExternalLabel(
                        "is_large_form_factor",
                        returnIsLargeFormFactorLabel
                    )
                )
            }
        }
    }
}
