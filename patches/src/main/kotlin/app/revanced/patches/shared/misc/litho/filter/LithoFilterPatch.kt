@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.shared.misc.litho.filter

import app.revanced.com.android.tools.smali.dexlib2.iface.value.MutableEncodedValue.Companion.toMutable
import app.revanced.patcher.classDef
import app.revanced.util.getFreeRegisterProvider
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.litho.context.EXTENSION_CONTEXT_INTERFACE
import app.revanced.patches.shared.misc.litho.context.conversionContextPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
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

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/patches/litho/LithoFilterPatch;"

/**
 * A patch that allows to filter Litho components based on their identifier or path.
 *
 * @param insertLegacyProtobufHook Hook legacy protobuf buffer into the extension to be used for filtering for older versions of the app.
 * @param getExtractIdentifierFromBuffer Whether to extract the identifier from the protobuf buffer.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */
internal fun lithoFilterPatch(
    insertLegacyProtobufHook: BytecodePatchContext.() -> Unit,
    executeBlock: BytecodePatchContext.() -> Unit = {},
    getExtractIdentifierFromBuffer: () -> Boolean = { false },
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
) {
    dependsOn(sharedExtensionPatch(), conversionContextPatch)

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
     *        ExtensionClass.setProtobugBuffer(byteBuffer); // Inserted by this patch.
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
        lithoFilterInitMethod.apply {
            // Remove the array initialization with the dummy filter.
            removeInstructions(6)

            addInstructions(
                0,
                "new-array v1, v1, [Lapp/revanced/extension/shared/patches/litho/Filter;"
            )

            // Fill the array with the filters added during patching.
            addLithoFilter = { classDescriptor ->
                addInstructions(
                    1,
                    """
                        new-instance v0, $classDescriptor
                        invoke-direct { v0 }, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v0, v1, v2
                    """,
                )
            }
        }

        // region Pass the buffer into extension.

        insertLegacyProtobufHook()

        // endregion

        // region Modify the create component method and
        // if the component is filtered then return an empty component.

        val builderMethodDescriptor =
            emptyComponentParentMethod.immutableClassDef.getEmptyComponentMethod()

        val emptyComponentField = firstClassDef(builderMethodDescriptor.returnType).fields.single()

        // Find the method call that gets the value of 'buttonViewModel.accessibilityId'.
        val accessibilityIdMethod = accessibilityIdMethodMatch.let {
            it.method.getInstruction<ReferenceInstruction>(it[0]).methodReference!!
        }

        // There's a method in the same class that gets the value of 'buttonViewModel.accessibilityText'.
        // As this class is abstract, another method that uses a method call is used.
        val accessibilityTextMethod = getAccessibilityTextMethodMatch(accessibilityIdMethod).let {
            // Find the method call that gets the value of 'buttonViewModel.accessibilityText'.
            it.method.getInstruction<ReferenceInstruction>(it[0]).methodReference
        }

        getComponentCreateMethodMatch(accessibilityIdMethod).let {
            val insertIndex = it[2]
            val buttonViewModelIndex = it[1]
            val nullCheckIndex = it[0]

            val buttonViewModelRegister =
                it.method.getInstruction<OneRegisterInstruction>(buttonViewModelIndex).registerA
            val accessibilityIdIndex = buttonViewModelIndex + 2

            val registerProvider = it.method.getFreeRegisterProvider(
                insertIndex, 3, buttonViewModelRegister
            )
            val contextRegister = registerProvider.getFreeRegister()
            val bufferRegister = registerProvider.getFreeRegister()
            val freeRegister = registerProvider.getFreeRegister()


            // Find a free register to store the accessibilityId and accessibilityText.
            // This is before the insertion index.
            val accessibilityRegisterProvider = it.method.getFreeRegisterProvider(
                nullCheckIndex,
                2,
                registerProvider.getUsedAndUnAvailableRegisters()
            )
            val accessibilityIdRegister = accessibilityRegisterProvider.getFreeRegister()
            val accessibilityTextRegister = accessibilityRegisterProvider.getFreeRegister()

            it.method.addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                   move-object/from16 v$bufferRegister, p3

                    # Verify it's the expected subclass just in case.
                    instance-of v$freeRegister, v$bufferRegister, ${protobufBufferEncodeMethod.definingClass}
                    if-eqz v$freeRegister, :empty_buffer

                    check-cast v$bufferRegister, ${protobufBufferEncodeMethod.definingClass}
                    invoke-virtual { v$bufferRegister }, $protobufBufferEncodeMethod
                    move-result-object v$bufferRegister
                    goto :hook

                    :empty_buffer
                    const/4 v$freeRegister, 0x0
                    new-array v$bufferRegister, v$freeRegister, [B

                    :hook
                    move-object/from16 v$contextRegister, p2
                    invoke-static { v$contextRegister, v$bufferRegister, v$accessibilityIdRegister, v$accessibilityTextRegister }, $EXTENSION_CLASS_DESCRIPTOR->isFiltered(${EXTENSION_CONTEXT_INTERFACE}[BLjava/lang/String;Ljava/lang/String;)Z
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :unfiltered
                    
                    # Return an empty component.
                    move-object/from16 v$freeRegister, p1
                    invoke-static { v$freeRegister }, $builderMethodDescriptor
                    move-result-object v$freeRegister
                    iget-object v$freeRegister, v$freeRegister, $emptyComponentField
                    return-object v$freeRegister
        
                    :unfiltered
                    nop
                """
            )

            // If there is text related to accessibility, get the accessibilityId and accessibilityText.
            it.method.addInstructions(
                accessibilityIdIndex,
                """
                    # Get accessibilityId
                    invoke-interface { v$buttonViewModelRegister }, $accessibilityIdMethod
                    move-result-object v$accessibilityIdRegister
                    
                    # Get accessibilityText
                    invoke-interface { v$buttonViewModelRegister }, $accessibilityTextMethod
                    move-result-object v$accessibilityTextRegister
                """
            )

            // If there is no accessibility-related text,
            // both accessibilityId and accessibilityText use empty values.
            it.method.addInstructions(
                nullCheckIndex,
                """
                    const-string v$accessibilityIdRegister, ""
                    const-string v$accessibilityTextRegister, ""
                """
            )
        }

        if (getExtractIdentifierFromBuffer()) {
            lithoFilterInitMethod.classDef.fields.first { it.name == "EXTRACT_IDENTIFIER_FROM_BUFFER" }
                .initialValue = ImmutableBooleanEncodedValue.forBoolean(true).toMutable()
        }

        // endregion

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
        // Set the array size to the actual filter count of the array
        // initialized at the beginning of the patch.
        lithoFilterInitMethod.addInstructions(0, "const/16 v1, $filterCount")
    }

    block()
}
