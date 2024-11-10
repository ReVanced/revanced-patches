@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_18_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.*
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

lateinit var addLithoFilter: (String) -> Unit
    private set

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/components/LithoFilterPatch;"

val lithoFilterPatch = bytecodePatch(
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
     * The following pseudocode shows how the patch works:
     *
     * class SomeOtherClass {
     *    // Called before ComponentContextParser.parseBytesToComponentContext method.
     *    public void someOtherMethod(ByteBuffer byteBuffer) {
     *        ExtensionClass.setProtoBuffer(byteBuffer); // Inserted by this patch.
     *        ...
     *   }
     * }
     *
     * When patching 19.17 and earlier:
     *
     * class ComponentContextParser {
     *    public ComponentContext ReadComponentIdentifierFingerprint(...) {
     *        ...
     *        if (extensionClass.filter(identifier, pathBuilder)); // Inserted by this patch.
     *            return emptyComponent;
     *        ...
     *    }
     * }
     *
     * When patching 19.18 and later:
     *
     * class ComponentContextParser {
     *    public ComponentContext parseBytesToComponentContext(...) {
     *        ...
     *        if (ReadComponentIdentifierFingerprint() == null); // Inserted by this patch.
     *            return emptyComponent;
     *        ...
     *    }
     *
     *    public ComponentIdentifierObj readComponentIdentifier(...) {
     *        ...
     *        if (extensionClass.filter(identifier, pathBuilder)); // Inserted by this patch.
     *            return null;
     *        ...
     *    }
     * }
     */
    execute {
        // Remove dummy filter from extenion static field
        // and add the filters included during patching.
        lithoFilterFingerprint.method().apply {
            removeInstructions(2, 4) // Remove dummy filter.

            addLithoFilter = { classDescriptor ->
                addInstructions(
                    2,
                    """
                        new-instance v1, $classDescriptor
                        invoke-direct {v1}, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v1, v0, v2
                    """,
                )
            }
        }

        // region Pass the buffer into extension.

        protobufBufferReferenceFingerprint.method().addInstruction(
            0,
            " invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )

        // endregion

        // region Hook the method that parses bytes into a ComponentContext.

        val readComponentMethod = readComponentIdentifierFingerprint.originalMethod()
        // Get the only static method in the class.
        val builderMethodDescriptor = emptyComponentFingerprint.classDef().methods.first { method ->
            AccessFlags.STATIC.isSet(method.accessFlags)
        }
        // Only one field.
        val emptyComponentField = classBy { classDef ->
            builderMethodDescriptor.returnType == classDef.type
        }!!.immutableClass.fields.single()

        // Returns an empty component instead of the original component.
        fun createReturnEmptyComponentInstructions(register: Int): String =
            """
                move-object/from16 v$register, p1
                invoke-static { v$register }, $builderMethodDescriptor
                move-result-object v$register
                iget-object v$register, v$register, $emptyComponentField
                return-object v$register
            """

        componentContextParserFingerprint.method().apply {
            // 19.18 and later require patching 2 methods instead of one.
            // Otherwise the modifications done here are the same for all targets.
            if (is_19_18_or_greater) {
                // Get the method name of the ReadComponentIdentifierFingerprint call.
                val readComponentMethodCallIndex = indexOfFirstInstructionOrThrow {
                    val reference = getReference<MethodReference>()
                    reference?.definingClass == readComponentMethod.definingClass &&
                        reference.name == readComponentMethod.name
                }

                // Result of read component, and also a free register.
                val register = getInstruction<OneRegisterInstruction>(readComponentMethodCallIndex + 1).registerA

                // Insert after 'move-result-object'
                val insertHookIndex = readComponentMethodCallIndex + 2

                // Return an EmptyComponent instead of the original component if the filterState method returns true.
                addInstructionsWithLabels(
                    insertHookIndex,
                    """
                        if-nez v$register, :unfiltered

                        # Component was filtered in ReadComponentIdentifierFingerprint hook
                        ${createReturnEmptyComponentInstructions(register)}
                    """,
                    ExternalLabel("unfiltered", getInstruction(insertHookIndex)),
                )
            }
        }

        // endregion

        // region Read component then store the result.

        readComponentIdentifierFingerprint.method().apply {
            val insertHookIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.IPUT_OBJECT &&
                    getReference<FieldReference>()?.type == "Ljava/lang/StringBuilder;"
            }
            val stringBuilderRegister = getInstruction<TwoRegisterInstruction>(insertHookIndex).registerA

            // Identifier is saved to a field just before the string builder.
            val identifierRegister = getInstruction<TwoRegisterInstruction>(
                indexOfFirstInstructionReversedOrThrow(insertHookIndex) {
                    opcode == Opcode.IPUT_OBJECT &&
                        getReference<FieldReference>()?.type == "Ljava/lang/String;"
                },
            ).registerA

            // Find a free temporary register.
            val freeRegister = getInstruction<OneRegisterInstruction>(
                // Immediately before is a StringBuilder append constant character.
                indexOfFirstInstructionReversedOrThrow(insertHookIndex, Opcode.CONST_16),
            ).registerA

            // Verify the temp register will not clobber the method result register.
            if (stringBuilderRegister == freeRegister) {
                throw PatchException("Free register will clobber StringBuilder register")
            }

            val invokeFilterInstructions = """
                invoke-static { v$identifierRegister, v$stringBuilderRegister }, $EXTENSION_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                move-result v$freeRegister
                if-eqz v$freeRegister, :unfiltered
            """

            addInstructionsWithLabels(
                insertHookIndex,
                if (is_19_18_or_greater) {
                    """
                        $invokeFilterInstructions
                        
                        # Return null, and the ComponentContextParserFingerprint hook 
                        # handles returning an empty component.
                        const/4 v$freeRegister, 0x0
                        return-object v$freeRegister
                    """
                } else {
                    """
                        $invokeFilterInstructions
                        
                        ${createReturnEmptyComponentInstructions(freeRegister)}
                    """
                },
                ExternalLabel("unfiltered", getInstruction(insertHookIndex)),
            )
        }

        // endregion

        // region A/B test of new Litho native code.

        // Turn off native code that handles litho component names.  If this feature is on then nearly
        // all litho components have a null name and identifier/path filtering is completely broken.
        if (is_19_25_or_greater) {
            lithoComponentNameUpbFeatureFlagFingerprint.method().apply {
                // Don't use return early, so the debug patch logs if this was originally on.
                val insertIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN)
                val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(insertIndex, "const/4 v$register, 0x0")
            }
        }

        // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
        // If this is enabled, then the litho protobuffer hook will always show an empty buffer
        // since it's no longer handled by the hooked Java code.
        lithoConverterBufferUpbFeatureFlagFingerprint.method().apply {
            val index = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(index + 1, "const/4 v$register, 0x0")
        }

        // endregion
    }

    finalize {
        lithoFilterFingerprint.method().replaceInstruction(0, "const/16 v0, $filterCount")
    }
}
