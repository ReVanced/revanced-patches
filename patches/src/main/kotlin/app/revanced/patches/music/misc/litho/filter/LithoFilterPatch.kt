package app.revanced.patches.music.misc.litho.filter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.shared.conversionContextFingerprintToString
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Used to add a hook point to the extension stub.
 */
lateinit var addLithoFilter: (String) -> Unit
    private set

/**
 * Counts the number of filters added to the static field array.
 */
private var filterCount = 0

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/patches/litho/LithoFilterPatch;"

val lithoFilterPatch = bytecodePatch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
) {
    dependsOn(
        sharedExtensionPatch
    )

    execute {
        // Remove dummy filter from extension static field
        // and add the filters included during patching.
        lithoFilterFingerprint.method.apply {
            removeInstructions(2, 4) // Remove dummy filter.

            addLithoFilter = { classDescriptor ->
                addInstructions(
                    2,
                    """
                        new-instance v1, $classDescriptor
                        invoke-direct { v1 }, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v1, v0, v2
                    """,
                )
            }
        }

        // Add an interceptor to steal the protobuf of our component.
        protobufBufferReferenceFingerprint.method.addInstruction(
            0,
            "invoke-static { p2 }, ${EXTENSION_CLASS_DESCRIPTOR}->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )

        // Hook the method that parses bytes into a ComponentContext.
        // Allow the method to run to completion, and override the
        // return value with an empty component if it should be filtered.
        // It is important to allow the original code to always run to completion,
        // otherwise high memory usage and poor app performance can occur.

        // Find the identifier/path class and fields of the conversion context.
        val conversionContextIdentifierField = componentContextParserFingerprint.let {
            // Identifier field is loaded just before the string declaration.
            val index = it.method.indexOfFirstInstructionReversedOrThrow(
                it.stringMatches!!.first().index
            ) {
                // Our instruction reads a String from a field of the ConversionContext class.
                val reference = getReference<FieldReference>()
                reference?.definingClass == conversionContextFingerprintToString.originalClassDef.type
                        && reference.type == "Ljava/lang/String;"
            }

            it.method.getInstruction<ReferenceInstruction>(index).getReference<FieldReference>()!!
        }

        val conversionContextPathBuilderField = conversionContextFingerprintToString.originalClassDef
            .fields.single { field -> field.type == "Ljava/lang/StringBuilder;" }

        // Find class and methods to create an empty component.
        val builderMethodDescriptor = emptyComponentFingerprint.classDef.methods.single {
            // The only static method in the class.
                method ->
            AccessFlags.STATIC.isSet(method.accessFlags)
        }

        val emptyComponentField = classBy {
            // Only one field in this class.
            it.type == builderMethodDescriptor.returnType
        }!!.immutableClass.fields.single()

        // Match all component creations methods
        componentCreateFingerprint.method.apply {
            // No supported version clobbers p2 so we can just do our things before the return instruction.
            val insertIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
            val freeRegister = findFreeRegister(insertIndex)
            val identifierRegister = findFreeRegister(insertIndex, freeRegister)
            val pathRegister = findFreeRegister(insertIndex, freeRegister, identifierRegister)

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    move-object/from16 v$freeRegister, p2 # ConversionContext parameter
                    check-cast v$freeRegister, ${conversionContextFingerprintToString.originalClassDef.type} # Check we got the actual ConversionContext
                    
                    iget-object v$identifierRegister, v$freeRegister, $conversionContextIdentifierField
                    iget-object v$pathRegister, v$freeRegister, $conversionContextPathBuilderField
                    
                    # Check if the component should be filtered.
                    invoke-static { v$identifierRegister, v$pathRegister }, ${EXTENSION_CLASS_DESCRIPTOR}->isFiltered(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :unfiltered
                    
                    # Return an empty component
                    move-object/from16 v$freeRegister, p1
                    invoke-static { v$freeRegister }, $builderMethodDescriptor
                    move-result-object v$freeRegister
                    iget-object v$freeRegister, v$freeRegister, $emptyComponentField
                    return-object v$freeRegister
        
                    :unfiltered
                    nop
                """
            )
        }

        // TODO: Check if needed in music
        // Change Litho thread executor to 1 thread to fix layout issue in unpatched YouTube.
        lithoThreadExecutorFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, ${EXTENSION_CLASS_DESCRIPTOR}->getExecutorCorePoolSize(I)I
                move-result p1
                invoke-static { p2 }, ${EXTENSION_CLASS_DESCRIPTOR}->getExecutorMaxThreads(I)I
                move-result p2
            """
        )


    }

    finalize {
        // Save the number of filters added.
        lithoFilterFingerprint.method.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}