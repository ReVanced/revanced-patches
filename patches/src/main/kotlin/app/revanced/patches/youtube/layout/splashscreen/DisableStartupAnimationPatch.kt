package app.revanced.patches.youtube.layout.splashscreen

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/DisableStartupAnimationPatch;"

@Suppress("unused")
val disableStartupAnimationPatch = bytecodePatch(
    name = "Disable startup animation",
    description = "Adds an option to disable the splash screen animation on app startup.",
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
        addResources("youtube", "layout.splashscreen.disableStartupAnimationPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_disable_startup_animation")
        )

        val startUpResourceCall = startUpResourceIdFingerprint.match(
            startUpResourceIdParentFingerprint.classDef
        ).originalMethod

        mainActivityOnCreateFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.definingClass == startUpResourceCall.definingClass
                        && reference.name == startUpResourceCall.name
                        && reference.parameterTypes == startUpResourceCall.parameterTypes
            }.forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                addInstructions(
                    index + 2,
                    """
                        invoke-static {v$register}, $EXTENSION_CLASS_DESCRIPTOR->showStartupAnimation(Z)Z
                        move-result v$register
                    """
                )
            }
        }
    }
}
