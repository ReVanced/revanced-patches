@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.shared.misc.litho.filter

import app.revanced.com.android.tools.smali.dexlib2.iface.value.MutableEncodedValue.Companion.toMutable
import app.revanced.patcher.classDef
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.conversionContextToStringMethod
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.findFieldFromToString
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.immutable.value.ImmutableBooleanEncodedValue

/**
 * Used to add a hook point to the extension stub.
 */
lateinit var addLithoFilter: (String) -> Unit
    private set

/**
 * Counts the number of filters added to the static field array.
 */
private var filterCount = 0

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/shared/patches/litho/LithoFilterPatch;"

/**
 * A patch that allows to filter Litho components based on their identifier or path.
 *
 * @param componentCreateInsertionIndex The index to insert the filtering code in the component create method.
 * @param insertProtobufHook This method injects a setProtoBuffer call in the protobuf decoding logic.
 * @param getExtractIdentifierFromBuffer Whether to extract the identifier from the protobuf buffer.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
internal fun lithoFilterPatch(
    componentCreateInsertionIndex: Method.() -> Int,
    insertProtobufHook: BytecodePatchContext.() -> Unit,
    executeBlock: BytecodePatchContext.() -> Unit = {},
    getExtractIdentifierFromBuffer: () -> Boolean = { false },
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
    apply {
        // Remove dummy filter from extenion static field
        // and add the filters included during patching.
        lithoFilterMethod.apply {
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

        if (getExtractIdentifierFromBuffer()) {
            lithoFilterMethod.classDef.fields.first { it.name == "EXTRACT_IDENTIFIER_FROM_BUFFER" }
                .initialValue = ImmutableBooleanEncodedValue.forBoolean(true).toMutable()
        }

        // Add an interceptor to steal the protobuf of our component.
        insertProtobufHook()

        // Hook the method that parses bytes into a ComponentContext.
        // Allow the method to run to completion, and override the
        // return value with an empty component if it should be filtered.
        // It is important to allow the original code to always run to completion,
        // otherwise high memory usage and poor app performance can occur.

        // Find the identifier/path fields of the conversion context.
        val conversionContextIdentifierField = conversionContextToStringMethod
            .findFieldFromToString("identifierProperty=")

        val conversionContextPathBuilderField = conversionContextToStringMethod.immutableClassDef
            .fields.single { field -> field.type == "Ljava/lang/StringBuilder;" }

        // Find class and methods to create an empty component.
        val builderMethodDescriptor = emptyComponentMethod.immutableClassDef.methods.single {
                // The only static method in the class.
                method ->
            AccessFlags.STATIC.isSet(method.accessFlags)
        }

        val emptyComponentField = firstClassDef {
            // Only one field that matches.
            type == builderMethodDescriptor.returnType
        }.fields.single()

        // Match all component creations methods
        componentCreateMethod.apply {
            val insertIndex = componentCreateInsertionIndex()
            val freeRegister = findFreeRegister(insertIndex)
            val identifierRegister = findFreeRegister(insertIndex, freeRegister)
            val pathRegister = findFreeRegister(insertIndex, freeRegister, identifierRegister)

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    move-object/from16 v$freeRegister, p2 # ConversionContext parameter
                    
                    # In YT 20.41 the field is the abstract superclass.
                    # Check it's the actual ConversionContext just in case. 
                    instance-of v$identifierRegister, v$freeRegister, ${conversionContextToStringMethod.immutableClassDef.type}
                    if-eqz v$identifierRegister, :unfiltered
                    
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
                """,
            )
        }

        // TODO: Check if needed in music.
        // Change Litho thread executor to 1 thread to fix layout issue in unpatched YouTube.
        lithoThreadExecutorMethod.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->getExecutorCorePoolSize(I)I
                move-result p1
                invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->getExecutorMaxThreads(I)I
                move-result p2
            """,
        )

        executeBlock()
    }

    afterDependents {
        // Save the number of filters added.
        lithoFilterMethod.replaceInstruction(0, "const/16 v0, $filterCount")
    }

    block()
}
