@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.litho.filter.EXTENSION_CLASS_DESCRIPTOR
import app.revanced.patches.shared.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.shared.misc.litho.filter.protobufBufferReferenceLegacyMethod
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.patches.youtube.shared.conversionContextToStringMethod
import app.revanced.util.*
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

lateinit var addLithoFilter: (String) -> Unit
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/components/LithoFilterPatch;"

val lithoFilterPatch = lithoFilterPatch(
    componentCreateInsertionIndex = {
        if (is_19_17_or_greater) {
            indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
        } else {
            // 19.16 clobbers p2 so must check at start of the method and not at the return index.
            0
        }
    },
    insertProtobufHook = {
        if (is_20_22_or_greater) {
            // Hook method that bridges between UPB buffer native code and FB Litho.
            // Method is found in 19.25+, but is forcefully turned off for 20.21 and lower.
            protobufBufferReferenceMethodMatch.let {
                // Hook the buffer after the call to jniDecode().
                it.method.addInstruction(
                    it[-1] + 1,
                    "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer([B)V",
                )
            }
        }

        // Legacy Non native buffer.
        protobufBufferReferenceLegacyMethod.addInstruction(
            0,
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )
    },
    executeBlock = {
        // region A/B test of new Litho native code.

        // Turn off native code that handles litho component names.  If this feature is on then nearly
        // all litho components have a null name and identifier/path filtering is completely broken.
        //
        // Flag was removed in 20.05. It appears a new flag might be used instead (45660109L),
        // but if the flag is forced on then litho filtering still works correctly.
        if (is_19_25_or_greater && !is_20_05_or_greater) {
            lithoComponentNameUpbFeatureFlagMethod.returnLate(false)
        }

        // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
        lithoConverterBufferUpbFeatureFlagMethodMatch.let {
            // 20.22 the flag is still enabled in one location, but what it does is not known.
            // Disable it anyway.
            it.method.insertLiteralOverride(
                it[0],
                false,
            )
        }

        // endregion
    }
) {
    dependsOn(sharedExtensionPatch, versionCheckPatch)
}

val lithoFilterPatchOld = bytecodePatch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
) {
    dependsOn(
        sharedExtensionPatch,
        versionCheckPatch,
    )

    var filterCount = 0

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
     * class ComponentContextParser {
     *    public Component parseComponent() {
     *        ...
     *
     *        if (extensionClass.shouldFilter()) {  // Inserted by this patch.
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

        // region Pass the buffer into extension.

        if (is_20_22_or_greater) {
            // Hook method that bridges between UPB buffer native code and FB Litho.
            // Method is found in 19.25+, but is forcefully turned off for 20.21 and lower.
            protobufBufferReferenceMethodMatch.let {
                // Hook the buffer after the call to jniDecode().
                it.method.addInstruction(
                    it[-1] + 1,
                    "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer([B)V",
                )
            }
        }

        // Legacy Non native buffer.
        protobufBufferReferenceLegacyMethod.addInstruction(
            0,
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )

        // endregion

        // region Modify the create component method and
        // if the component is filtered then return an empty component.

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

        val emptyComponentField = firstClassDef(builderMethodDescriptor.returnType).fields.single()

        componentCreateMethod.apply {
            val insertIndex = if (is_19_17_or_greater) {
                indexOfFirstInstructionOrThrow(Opcode.RETURN_OBJECT)
            } else {
                // 19.16 clobbers p2 so must check at start of the method and not at the return index.
                0
            }

            val freeRegister = findFreeRegister(insertIndex)
            val identifierRegister = findFreeRegister(insertIndex, freeRegister)
            val pathRegister = findFreeRegister(insertIndex, freeRegister, identifierRegister)

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                    move-object/from16 v$freeRegister, p2
                    
                    # 20.41 field is the abstract superclass.
                    # Verify it's the expected subclass just in case. 
                    instance-of v$identifierRegister, v$freeRegister, ${conversionContextToStringMethod.immutableClassDef.type}
                    if-eqz v$identifierRegister, :unfiltered
                    
                    iget-object v$identifierRegister, v$freeRegister, $conversionContextIdentifierField
                    iget-object v$pathRegister, v$freeRegister, $conversionContextPathBuilderField
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

        // endregion

        // region Change Litho thread executor to 1 thread to fix layout issue in unpatched YouTube.

        lithoThreadExecutorMethod.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->getExecutorCorePoolSize(I)I
                move-result p1
                invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->getExecutorMaxThreads(I)I
                move-result p2
            """,
        )

        // endregion

        // region A/B test of new Litho native code.

        // Turn off native code that handles litho component names.  If this feature is on then nearly
        // all litho components have a null name and identifier/path filtering is completely broken.
        //
        // Flag was removed in 20.05. It appears a new flag might be used instead (45660109L),
        // but if the flag is forced on then litho filtering still works correctly.
        if (is_19_25_or_greater && !is_20_05_or_greater) {
            lithoComponentNameUpbFeatureFlagMethod.returnLate(false)
        }

        // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
        lithoConverterBufferUpbFeatureFlagMethodMatch.let {
            // 20.22 the flag is still enabled in one location, but what it does is not known.
            // Disable it anyway.
            it.method.insertLiteralOverride(
                it[0],
                false,
            )
        }

        // endregion
    }

    afterDependents {
        lithoFilterMethod.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}
