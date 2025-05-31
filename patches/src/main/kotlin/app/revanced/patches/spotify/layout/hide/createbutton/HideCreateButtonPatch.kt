package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
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

    execute {
        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            Logger.getLogger(this::class.java.name).warning(
                "Create button does not exist in legacy app target.  No changes applied."
            )
            return@execute
        }

        // Add a check before each navigation bar item is added to the LinkedHashSet to check if the item being added
        // is the Create button, and in case it is, skip adding it.
        val navigationBarItemSetClassDef = navigationBarItemSetClassFingerprint.originalClassDef
        navigationBarItemSetConstructorFingerprint.match(navigationBarItemSetClassDef).method.let { method ->
            val navigationBarItemClassType = method.parameterTypes.first()

            method.instructions.forEachIndexed { instructionIndex, instruction ->
                if (
                    // The instruction which calls add should an invoke-virtual.
                    instruction as? FiveRegisterInstruction == null ||
                    // Method call to LinkedHashSet->add.
                    instruction.getReference<MethodReference>()?.name != "add" ||
                    // Check if the instruction before the current one is checking whether the current item is null.
                    method.getInstruction(instructionIndex - 1).opcode != Opcode.IF_EQZ
                ) {
                    return@forEachIndexed
                }

                val navigationBarItemsRegister = instruction.registerC
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
                        invoke-virtual { v$navigationBarItemRegister }, $navigationBarItemToStringDescriptor
                        move-result-object v$freeRegister
        
                        invoke-static { v$freeRegister }, $isCreateButtonDescriptor
                        move-result v$freeRegister
        
                        # If this the Create button, skip adding it and jump to the next method instruction.
                        if-nez v$freeRegister, :next-instruction
                    """,
                    ExternalLabel("next-instruction", nextInstruction)
                )
            }
        }
    }
}
