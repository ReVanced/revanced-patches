@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.shared.misc.litho.filter

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
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

/**
 * A patch that allows to filter Litho components based on their identifier or path.
 *
 * @param componentCreateInsertionIndex The index to insert the filtering code in the component create method.
 * @param conversionContextFingerprintToString The fingerprint of the conversion context to string method.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
internal fun lithoFilterPatch(
    componentCreateInsertionIndex: Method.() -> Int,
    conversionContextFingerprintToString: Fingerprint,
    executeBlock: BytecodePatchContext.() -> Unit = {},
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
) {
    dependsOn(
        sharedExtensionPatch(),
    )

    /**
     * The following patch inserts a hook into the method that parses the bytes into a ComponentContext.
     * This method contains a StringBuilder object that represents the pathBuilder of the component.
     * The pathBuilder is used to filter components by their path.
     *
     * Additionally, the method contains a reference to the component's identifier.
     * The identifier is used to filter components by their identifier.
     *
     * The protobuf buffer is passed along from a different injection point before the filtering occurs.
     * The buffer is a large byte array that represents the component tree.
     * This byte array is searched for strings that indicate the current component.
     *
     * All modifications done here must allow all the original code to still execute
     * even when filtering, otherwise memory leaks or poor app performance may occur.
     *
     * The following pseudocode shows how this patch works:
     *
     * class SomeOtherClass {
     *    // Called before ComponentContextParser.parseComponent() method.
     *    public void someOtherMethod(ByteBuffer byteBuffer) {
     *        ExtensionClass.setProtoBuffer(byteBuffer); // Inserted by this patch.
     *        ...
     *   }
     * }
     *
     * class CreateComponentClass {
     *    public Component createComponent() {
     *        ...
     *
     *        if (extensionClass.shouldFilter(identifier, path)) {  // Inserted by this patch.
     *            return emptyComponent;
     *        }
     *        return originalUnpatchedComponent; // Original code.
     *    }
     * }
     */
    execute {
        // Remove dummy filter from extenion static field
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
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )


        // Hook the method that parses bytes into a ComponentContext.
        // Allow the method to run to completion, and override the
        // return value with an empty component if it should be filtered.
        // It is important to allow the original code to always run to completion,
        // otherwise high memory usage and poor app performance can occur.

        // Find the identifier/path fields of the conversion context.
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
            // Only one field that matches.
            it.type == builderMethodDescriptor.returnType
        }!!.immutableClass.fields.single()

        // Match all component creations methods
        componentCreateFingerprint.method.apply {
            val insertIndex = componentCreateInsertionIndex()
            val freeRegister = findFreeRegister(insertIndex)
            val identifierRegister = findFreeRegister(insertIndex, freeRegister)
            val pathRegister = findFreeRegister(insertIndex, freeRegister, identifierRegister)

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    move-object/from16 v$freeRegister, p2 # ConversionContext parameter
                    check-cast v$freeRegister, ${conversionContextFingerprintToString.originalClassDef.type} # Check we got the actual ConversionContext
                    
                    # Get identifier and path from ConversionContext
                    iget-object v$identifierRegister, v$freeRegister, $conversionContextIdentifierField
                    iget-object v$pathRegister, v$freeRegister, $conversionContextPathBuilderField
                    
                    # Check if the component should be filtered.
                    invoke-static { v$identifierRegister, v$pathRegister }, $EXTENSION_CLASS_DESCRIPTOR->isFiltered(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
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
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->getExecutorCorePoolSize(I)I
                move-result p1
                invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->getExecutorMaxThreads(I)I
                move-result p2
            """
        )

        executeBlock()
    }

    finalize {
        // Save the number of filters added.
        lithoFilterFingerprint.method.replaceInstruction(0, "const/16 v0, $filterCount")
    }

    block()
}
