package app.revanced.patches.youtube.layout.hide.floatingmicrophone

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.hide.floatingmicrophone.fingerprints.showFloatingMicrophoneButtonFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/HideFloatingMicrophoneButtonPatch;"

@Suppress("unused")
val hideFloatingMicrophoneButtonPatch = bytecodePatch(
    name = "Hide floating microphone button",
    description = "Adds an option to hide the floating microphone button when searching.",
) {
    dependsOn(
        integrationsPatch,
        hideFloatingMicrophoneButtonResourcePatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.09.38",
            "19.10.39",
            "19.11.43",
        ),
    )

    val showFloatingMicrophoneButtonResult by showFloatingMicrophoneButtonFingerprint

    execute {
        showFloatingMicrophoneButtonResult.mutableMethod.apply {
            val insertIndex = showFloatingMicrophoneButtonResult.scanResult.patternScanResult!!.startIndex + 1
            val showButtonRegister =
                getInstruction<TwoRegisterInstruction>(insertIndex - 1).registerA

            addInstructions(
                insertIndex,
                """
                    invoke-static {v$showButtonRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->hideFloatingMicrophoneButton(Z)Z
                    move-result v$showButtonRegister
                """,
            )
        }
    }
}
