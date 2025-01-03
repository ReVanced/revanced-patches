package app.revanced.patches.youtube.ad.getpremium

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideGetPremiumPatch;"

val hideGetPremiumPatch = bytecodePatch(
    description = "Hides YouTube Premium signup promotions under the video player.",
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
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "ad.getpremium.hideGetPremiumPatch")

        PreferenceScreen.ADS.addPreferences(
            SwitchPreference("revanced_hide_get_premium"),
        )

        getPremiumViewFingerprint.method.apply {
            val startIndex = getPremiumViewFingerprint.filterMatches.first().index
            val measuredWidthRegister = getInstruction<TwoRegisterInstruction>(startIndex).registerA
            val measuredHeightInstruction = getInstruction<TwoRegisterInstruction>(startIndex + 1)

            val measuredHeightRegister = measuredHeightInstruction.registerA
            val tempRegister = measuredHeightInstruction.registerB

            addInstructionsWithLabels(
                startIndex + 2,
                """
                    # Override the internal measurement of the layout with zero values.
                    invoke-static {}, $EXTENSION_CLASS_DESCRIPTOR->hideGetPremiumView()Z
                    move-result v$tempRegister
                    if-eqz v$tempRegister, :allow
                    const/4 v$measuredWidthRegister, 0x0
                    const/4 v$measuredHeightRegister, 0x0
                    :allow
                    nop
                    # Layout width/height is then passed to a protected class method.
                """,
            )
        }
    }
}
