package app.revanced.patches.youtube.layout.formfactor

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.layout.buttons.navigation.navigationButtonsPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.navigation.hookNavigationButtonCreated
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ChangeFormFactorPatch;"

@Suppress("unused")
val changeFormFactorPatch = bytecodePatch(
    name = "Change form factor",
    description = "Adds an option to change the UI appearance to a phone, tablet, or automotive device.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        navigationButtonsPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.formfactor.changeFormFactorPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference("revanced_change_form_factor")
        )

        hookNavigationButtonCreated(EXTENSION_CLASS_DESCRIPTOR)

        createPlayerRequestBodyWithModelFingerprint.method.apply {
            val formFactorEnumClass = formFactorEnumConstructorFingerprint.originalClassDef.type

            val index = indexOfFirstInstructionOrThrow {
                val reference = getReference<FieldReference>()
                opcode == Opcode.IGET &&
                        reference?.definingClass == formFactorEnumClass &&
                        reference.type == "I"
            }
            val register = getInstruction<TwoRegisterInstruction>(index).registerA

            addInstructions(
                index + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getFormFactor(I)I
                    move-result v$register
                """
            )
        }
    }
}
