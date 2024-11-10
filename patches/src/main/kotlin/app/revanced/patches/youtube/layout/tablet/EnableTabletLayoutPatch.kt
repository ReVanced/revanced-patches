package app.revanced.patches.youtube.layout.tablet

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/TabletLayoutPatch;"

@Suppress("unused")
val enableTabletLayoutPatch = bytecodePatch(
    name = "Enable tablet layout",
    description = "Adds an option to enable tablet layout.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "layout.tablet.enableTabletLayoutPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_tablet_layout"),
        )

        getFormFactorFingerprint.method().apply {
            val returnIsLargeFormFactorIndex = instructions.lastIndex - 4
            val returnIsLargeFormFactorLabel = getInstruction(returnIsLargeFormFactorIndex)

            addInstructionsWithLabels(
                0,
                """
                      invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->getTabletLayoutEnabled()Z
                      move-result v0
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
