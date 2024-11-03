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
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.matchOrThrow
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
        lithoFilterFingerprint.matchOrThrow.method.apply {
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

        protobufBufferReferenceFingerprint.matchOrThrow.method.addInstruction(
            0,
            " invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )

        // endregion

        // region Hook the method that parses bytes into a ComponentContext.

        val readComponentMethod = readComponentIdentifierFingerprint.matchOrThrow.originalMethod
        // Get the only static method in the class.
        val builderMethodDescriptor = emptyComponentFingerprint.matchOrThrow.originalClassDef.methods.first { method ->
            AccessFlags.STATIC.isSet(method.accessFlags)
        }
        // Only one field.
        val emptyComponentField = classBy { classDef ->
            builderMethodDescriptor.returnType == classDef.type
        }!!.immutableClass.fields.single()

        componentContextParserFingerprint.matchOrThrow.method.apply {
            // 19.18 and later require patching 2 methods instead of one.
            // Otherwise, the patched code is the same.
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
                            move-object/from16 v$register, p1
                            invoke-static { v$register }, $builderMethodDescriptor
                            move-result-object v$register
                            iget-object v$register, v$register, $emptyComponentField
                            return-object v$register
                        """,
                    ExternalLabel("unfiltered", getInstruction(insertHookIndex)),
                )
            }
        }

        // endregion

        // region Read component then store the result.

        readComponentIdentifierFingerprint.matchOrThrow.method.apply {
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
            val register = getInstruction<OneRegisterInstruction>(
                // Immediately before is a StringBuilder append constant character.
                indexOfFirstInstructionReversedOrThrow(insertHookIndex, Opcode.CONST_16),
            ).registerA

            // Verify the temp register will not clobber the method result register.
            if (stringBuilderRegister == register) {
                throw PatchException("Free register will clobber StringBuilder register")
            }

            val invokeFilterInstructions = """
                invoke-static { v$identifierRegister, v$stringBuilderRegister }, $EXTENSION_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                move-result v$register
                if-eqz v$register, :unfiltered
            """

            addInstructionsWithLabels(
                insertHookIndex,
                if (is_19_18_or_greater) {
                    """
                        $invokeFilterInstructions
                        
                        # Return null, and the ComponentContextParserFingerprint hook 
                        # handles returning an empty component.
                        const/4 v$register, 0x0
                        return-object v$register
                    """
                } else {
                    """
                        $invokeFilterInstructions
                    
                        # Exact same code as ComponentContextParserFingerprint hook,
                        # but with the free register of this method.
                        move-object/from16 v$register, p1
                        invoke-static { v$register }, $builderMethodDescriptor
                        move-result-object v$register
                        iget-object v$register, v$register, $emptyComponentField
                        return-object v$register
                    """
                },
                ExternalLabel("unfiltered", getInstruction(insertHookIndex)),
            )
        }

        // endregion
    }

    finalize {
        lithoFilterFingerprint.matchOrThrow.method.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}
