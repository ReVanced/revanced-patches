package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.patches.spotify.shared.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.util.logging.Logger

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/spotify/layout/hide/createbutton/HideCreateButtonPatch;"

@Suppress("unused")
val hideCreateButtonPatch = bytecodePatch(
    name = "Hide Create button",
    description = "Hides the \"Create\" button in the navigation bar."
) {
    compatibleWith("com.spotify.music")

    dependsOn(sharedExtensionPatch)

    execute {
        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            Logger.getLogger(this::class.java.name).warning(
                "Create button does not exist in legacy app target.  No changes applied."
            )
            return@execute
        }

        val navigationBarItemSetClassDef = navigationBarItemSetClassFingerprint.originalClassDef

        // The NavigationBarItemSet constructor accepts multiple parameters which represent each navigation bar item.
        // Each item is manually checked whether it is not null and then added to a LinkedHashSet.
        // Since the order of the items can differ, we are required to iterate over all the instructions and add a check
        // before each item is added to the LinkedHasSet. Each item needs to be checked whether it is the Create
        // button, and in case it is, the code should jump to the next instruction and avoid adding it.
        navigationBarItemSetConstructorFingerprint.match(navigationBarItemSetClassDef).method.let { method ->
            val navigationBarItemClassType = method.parameterTypes.first()

            method.instructions.forEachIndexed { instructionIndex, instruction ->
                if (
                    // Check whether the instruction is an invoke-virtual.
                    instruction as? FiveRegisterInstruction == null ||
                    // Check whether the instruction does a method call to LinkedHashSet->add.
                    instruction.getReference<MethodReference>()?.name != "add" ||
                    // Check if the instruction before the current one is checking whether the current item is null.
                    method.getInstruction(instructionIndex - 1).opcode != Opcode.IF_EQZ
                ) {
                    return@forEachIndexed
                }

                // The LinkedHashSet register.
                val navigationBarItemsRegister = instruction.registerC
                // The current navigation bar item parameter register.
                val navigationBarItemRegister = instruction.registerD

                // Exclude the nagivationBarItemsRegister because the method calls LinkedHashSet->set on it.
                val freeRegister = method.findFreeRegister(0, navigationBarItemsRegister)

                // Either the instruction to add the next item or the rest of the method logic.
                val nextInstruction = method.getInstruction(instructionIndex + 1)

                val navigationBarItemToStringDescriptor = "$navigationBarItemClassType->toString()Ljava/lang/String;"
                val isCreateButtonDescriptor = "$EXTENSION_CLASS_DESCRIPTOR->isCreateButton(Ljava/lang/String;)Z"

                method.addInstructionsWithLabels(
                    instructionIndex,
                    """
                        # Stringify the navigation bar item.
                        invoke-virtual { v$navigationBarItemRegister }, $navigationBarItemToStringDescriptor
                        move-result-object v$freeRegister
        
                        # Check if this navigation bar item is the Create button.
                        invoke-static { v$freeRegister }, $isCreateButtonDescriptor
                        move-result v$freeRegister
        
                        # If this navigation bar item is the Create button,
                        # skip adding it and jump to the next method instruction.
                        if-nez v$freeRegister, :next-instruction
                    """,
                    ExternalLabel("next-instruction", nextInstruction)
                )
            }
        }
    }
}
