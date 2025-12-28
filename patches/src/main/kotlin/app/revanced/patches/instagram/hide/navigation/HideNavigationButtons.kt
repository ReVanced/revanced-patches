package app.revanced.patches.instagram.hide.navigation

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.booleanOption
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.instagram.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.PATCH_NAME_HIDE_NAVIGATION_BUTTONS
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/instagram/hide/navigation/HideNavigationButtonsPatch;"

@Suppress("unused")
val hideNavigationButtonsPatch = bytecodePatch(
    name = PATCH_NAME_HIDE_NAVIGATION_BUTTONS,
    description = "Hides navigation bar buttons, such as the Reels and Create button.",
    use = false
) {
    compatibleWith("com.instagram.android"("401.0.0.48.79"))

    dependsOn(sharedExtensionPatch)

    val hideHome by booleanOption(
        key = "hideHome",
        default = false,
        title = "Hide Home",
        description = "Permanently hides the Home button. App starts at next available tab." // On the "homecoming" / current instagram layout.
    )

    val hideReels by booleanOption(
        key = "hideReels",
        default = true,
        title = "Hide Reels",
        description = "Permanently hides the Reels button."
    )

    val hideDirect by booleanOption(
        key = "hideDirect",
        default = false,
        title = "Hide Direct",
        description = "Permanently hides the Direct button."
    )

    val hideSearch by booleanOption(
        key = "hideSearch",
        default = false,
        title = "Hide Search",
        description = "Permanently hides the Search button."
    )

    val hideProfile by booleanOption(
        key = "hideProfile",
        default = false,
        title = "Hide Profile",
        description = "Permanently hides the Profile button."
    )

    val hideCreate by booleanOption(
        key = "hideCreate",
        default = true,
        title = "Hide Create",
        description = "Permanently hides the Create button."
    )

    execute {
        if (!hideHome!! &&!hideReels!! && !hideDirect!! && !hideSearch!! && !hideProfile!! && !hideCreate!!) {
            return@execute Logger.getLogger(this::class.java.name).warning(
                "No hide navigation buttons options are enabled. No changes made."
            )
        }

        val enumNameField: String

        // Get the field name which contains the name of the enum for the navigation button ("fragment_clips", "fragment_share", ...)
        with(navigationButtonsEnumInitFingerprint.method) {
            enumNameField = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT &&
                        (this as TwoRegisterInstruction).registerA == 2 // The p2 register
            }.let {
                getInstruction(it).getReference<FieldReference>()!!.name
            }
        }

        initializeNavigationButtonsListFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
            val buttonsListRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA
            val freeRegister = findFreeRegister(returnIndex)
            val freeRegister2 = findFreeRegister(returnIndex, freeRegister)

            fun instructionsRemoveButtonByName(buttonEnumName: String): String {
                return """
                    const-string v$freeRegister, "$buttonEnumName"
                    const-string v$freeRegister2, "$enumNameField"
                    invoke-static { v$buttonsListRegister, v$freeRegister, v$freeRegister2 }, $EXTENSION_CLASS_DESCRIPTOR->removeNavigationButtonByName(Ljava/util/List;Ljava/lang/String;Ljava/lang/String;)Ljava/util/List;
                    move-result-object v$buttonsListRegister
                """
            }

            if (hideHome!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_feed")
                )
            }

            if (hideReels!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_clips")
                )
            }

            if (hideDirect!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_direct_tab")
                )
            }
            if (hideSearch!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_search")
                )
            }

            if (hideCreate!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_share")
                )
            }

            if (hideProfile!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_profile")
                )
            }

        }
    }
}
