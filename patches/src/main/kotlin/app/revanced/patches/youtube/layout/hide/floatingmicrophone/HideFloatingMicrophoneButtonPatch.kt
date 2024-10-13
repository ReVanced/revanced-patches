package app.revanced.patches.youtube.layout.hide.floatingmicrophone

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

internal var fabButtonId = -1L
    private set

private val hideFloatingMicrophoneButtonResourcePatch = resourcePatch {
    dependsOn(
        settingsPatch,
        resourceMappingPatch,
        addResourcesPatch,
    )

    execute {
        addResources("youtube", "layout.hide.floatingmicrophone.hideFloatingMicrophoneButtonResourcePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_hide_floating_microphone_button"),
        )

        fabButtonId = resourceMappings["id", "fab"]
    }
}

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/HideFloatingMicrophoneButtonPatch;"

@Suppress("unused")
val hideFloatingMicrophoneButtonPatch = bytecodePatch(
    name = "Hide floating microphone button",
    description = "Adds an option to hide the floating microphone button when searching.",
) {
    dependsOn(
        sharedExtensionPatch,
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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    val showFloatingMicrophoneButtonMatch by showFloatingMicrophoneButtonFingerprint()

    execute {
        showFloatingMicrophoneButtonMatch.mutableMethod.apply {
            val insertIndex = showFloatingMicrophoneButtonMatch.patternMatch!!.startIndex + 1
            val showButtonRegister =
                getInstruction<TwoRegisterInstruction>(insertIndex - 1).registerA

            addInstructions(
                insertIndex,
                """
                    invoke-static {v$showButtonRegister}, $EXTENSION_CLASS_DESCRIPTOR->hideFloatingMicrophoneButton(Z)Z
                    move-result v$showButtonRegister
                """,
            )
        }
    }
}
