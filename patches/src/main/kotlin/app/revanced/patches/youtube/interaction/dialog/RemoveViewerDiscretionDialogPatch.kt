package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.accessFlags
import app.revanced.patcher.custom
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.firstMethodDeclaratively
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.backgroundPlaybackManagerShortsMethod
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/RemoveViewerDiscretionDialogPatch;"

@Suppress("unused")
val removeViewerDiscretionDialogPatch = bytecodePatch(
    name = "Remove viewer discretion dialog",
    description = "Adds an option to remove the dialog that appears when opening a video that has been age-restricted " +
            "by accepting it automatically. This does not bypass the age restriction.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
        addResources("youtube", "interaction.dialog.removeViewerDiscretionDialogPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_remove_viewer_discretion_dialog"),
        )

        createDialogMethodMatch.let {
            it.method.apply {
                val showDialogIndex = it[-1]
                val dialogRegister =
                    getInstruction<FiveRegisterInstruction>(showDialogIndex).registerC

                replaceInstructions(
                    showDialogIndex,
                    "invoke-static { v$dialogRegister }, ${EXTENSION_CLASS_DESCRIPTOR}->" +
                            "confirmDialog(Landroid/app/AlertDialog;)V",
                )
            }
        }

        createModernDialogMethodMatch.let {
            it.method.apply {
                val showDialogIndex = it[-1]
                val dialogRegister =
                    getInstruction<FiveRegisterInstruction>(showDialogIndex).registerC

                replaceInstructions(
                    showDialogIndex,
                    "invoke-static { v$dialogRegister }, ${EXTENSION_CLASS_DESCRIPTOR}->" +
                            "confirmDialog(Landroid/app/AlertDialog\$Builder;)Landroid/app/AlertDialog;",
                )

                val dialogStyleIndex = it[0]
                val dialogStyleRegister =
                    getInstruction<OneRegisterInstruction>(dialogStyleIndex).registerA

                addInstructions(
                    dialogStyleIndex + 1,
                    """
                        invoke-static { v$dialogStyleRegister }, ${EXTENSION_CLASS_DESCRIPTOR}->disableModernDialog(Z)Z
                        move-result v$dialogStyleRegister
                    """
                )
            }
        }

        backgroundPlaybackManagerShortsMethod.immutableClassDef.firstMethodDeclaratively {
            accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
            returnType("Z")
            parameterTypes(playabilityStatusEnumMethod.immutableClassDef.type)
            custom {
                // There's another similar method that's difficult to match uniquely,
                // Instruction counter is used to identify the target method.
                instructions.count() < 10
            }
        }.addInstruction(
            0,
            "invoke-static { p0 }, ${EXTENSION_CLASS_DESCRIPTOR}->" +
                    "setPlayabilityStatus(Ljava/lang/Enum;)V"
        )
    }
}
