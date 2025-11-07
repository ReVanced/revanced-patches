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

    val hideReels by booleanOption(
        key = "hideReels",
        default = true,
        title = "Hide Reels",
        description = "Permanently hides the Reels button."
    )

    val hideCreate by booleanOption(
        key = "hideCreate",
        default = true,
        title = "Hide Create",
        description = "Permanently hides the Create button."
    )

    execute {
        if (!hideReels!! && !hideCreate!!) {
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

            if (hideReels!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_clips")
                )
            }

            if (hideCreate!!) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    instructionsRemoveButtonByName("fragment_share")
                )
            }
        }
    }
}
