package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.tablet.fingerprints.getFormFactorFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val enableTabletLayoutPatch = bytecodePatch(
    name = "Enable tablet layout",
    description = "Adds an option to spoof the device form factor to a tablet which enables the tablet layout.",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith("com.google.android.youtube")

    val getFormFactorResult by getFormFactorFingerprint

    execute {
        addResources("youtube", "layout.tablet.EnableTabletLayoutPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_layout"),
        )

        getFormFactorResult.mutableMethod.apply {
            val returnIsLargeFormFactorIndex = getInstructions().lastIndex - 4
            val returnIsLargeFormFactorLabel = getInstruction(returnIsLargeFormFactorIndex)

            addInstructionsWithLabels(
                0,
                """
                      invoke-static { }, Lapp/revanced/integrations/youtube/patches/EnableTabletLayoutPatch;->enableTabletLayout()Z
                      move-result v0 # Free register
                      if-nez v0, :is_large_form_factor
                """,
                ExternalLabel(
                    "is_large_form_factor",
                    returnIsLargeFormFactorLabel,
                ),
            )
        }
    }
}
