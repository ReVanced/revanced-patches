package app.revanced.patches.youtube.layout.formfactor

import app.revanced.patcher.InstructionLocation.*
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.fieldAccess
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.youtube.layout.buttons.navigation.`Navigation buttons`
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.navigation.hookNavigationButtonCreated
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ChangeFormFactorPatch;"

@Suppress("unused")
val `Change form factor` by creatingBytecodePatch(
    description = "Adds an option to change the UI appearance to a phone, tablet, or automotive device.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        `Navigation buttons`,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        ),
    )

    apply {
        addResources("youtube", "layout.formfactor.changeFormFactorPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            ListPreference("revanced_change_form_factor"),
        )

        hookNavigationButtonCreated(EXTENSION_CLASS_DESCRIPTOR)

        val createPlayerRequestBodyWithModelFingerprint = fingerprint {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
            returnType("L")
            parameterTypes()
            instructions(
                fieldAccess(smali = "Landroid/os/Build;->MODEL:Ljava/lang/String;"),
                fieldAccess(
                    definingClass = formFactorEnumConstructorMethod.originalClassDef.type,
                    type = "I",
                    location = MatchAfterWithin(50),
                ),
            )
        }

        createPlayerRequestBodyWithModelFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.last().index
                val register = getInstruction<TwoRegisterInstruction>(index).registerA

                addInstructions(
                    index + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getFormFactor(I)I
                        move-result v$register
                    """,
                )
            }
        }
    }
}
