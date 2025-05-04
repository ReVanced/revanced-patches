@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.revanced.patches.youtube.layout.returnyoutubedislike.conversionContextFingerprint
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_18_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_05_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversed
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField

lateinit var addLithoFilter: (String) -> Unit
    private set

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/components/LithoFilterPatch;"

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
     * All modifications done here must allow all the original code to still execute
     * even when filtering, otherwise memory leaks or poor app performance may occur.
     *
     * The following pseudocode shows how this patch works:
     *
     * class SomeOtherClass {
     *    // Called before ComponentContextParser.readComponentIdentifier(...) method.
     *    public void someOtherMethod(ByteBuffer byteBuffer) {
     *        ExtensionClass.setProtoBuffer(byteBuffer); // Inserted by this patch.
     *        ...
     *   }
     * }
     *
     * When patching 19.16:
     *
     * class ComponentContextParser {
     *    public Component readComponentIdentifier(...) {
     *        ...
     *        if (extensionClass.filter(identifier, pathBuilder)) { // Inserted by this patch.
     *            return emptyComponent;
     *        }
     *        return originalUnpatchedComponent;
     *    }
     * }
     *
     * When patching 19.18 and later:
     *
     * class ComponentContextParser {
     *    public ComponentIdentifierObj readComponentIdentifier(...) {
     *        ...
     *        if (extensionClass.filter(identifier, pathBuilder)) { // Inserted by this patch.
     *            this.patch_isFiltered = true;
     *        }
     *        ...
     *    }
     *
     *    public Component parseBytesToComponentContext(...) {
     *        ...
     *        if (this.patch_isFiltered) { // Inserted by this patch.
     *            return emptyComponent;
     *        }
     *        return originalUnpatchedComponent;
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
                        invoke-direct {v1}, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v1, v0, v2
                    """,
                )
            }
        }

        // region Pass the buffer into extension.

        protobufBufferReferenceFingerprint.method.addInstruction(
            0,
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )

        // endregion

        // region Hook the method that parses bytes into a ComponentContext.

        // Get the only static method in the class.
        val builderMethodDescriptor = emptyComponentFingerprint.classDef.methods.first { method ->
            AccessFlags.STATIC.isSet(method.accessFlags)
        }
        // Only one field.
        val emptyComponentField = classBy { classDef ->
            builderMethodDescriptor.returnType == classDef.type
        }!!.immutableClass.fields.single()

        // Add a field to store the result of the filtering. This allows checking the field
        // just before returning so the original code always runs the same when filtering occurs.
        val lithoFilterResultField = ImmutableField(
            componentContextParserFingerprint.classDef.type,
            "patch_isFiltered",
            "Z",
            AccessFlags.PRIVATE.value,
            null,
            null,
            null,
        ).toMutable()
        componentContextParserFingerprint.classDef.fields.add(lithoFilterResultField)

        // Returns an empty component instead of the original component.
        fun createReturnEmptyComponentInstructions(free: Int): String = """
            move-object/from16 v$free, p0
            iget-boolean v$free, v$free, $lithoFilterResultField
            if-eqz v$free, :unfiltered
            
            move-object/from16 v$free, p1
            invoke-static { v$free }, $builderMethodDescriptor
            move-result-object v$free
            iget-object v$free, v$free, $emptyComponentField
            return-object v$free
            
            :unfiltered
            nop
        """

        componentContextParserFingerprint.method.apply {
            // 19.18 and later require patching 2 methods instead of one.
            // Otherwise the modifications done here are the same for all targets.
            if (is_19_18_or_greater) {
                findInstructionIndicesReversedOrThrow(Opcode.RETURN_OBJECT).forEach { index ->
                    val free = findFreeRegister(index)

                    addInstructionsAtControlFlowLabel(
                        index,
                        createReturnEmptyComponentInstructions(free)
                    )
                }
            }
        }

        // endregion

        // region Read component then store the result.

        readComponentIdentifierFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionReversedOrThrow(Opcode.RETURN_OBJECT)
            if (indexOfFirstInstructionReversed(returnIndex - 1, Opcode.RETURN_OBJECT) >= 0) {
                throw PatchException("Found multiple return indexes") // Patch needs an update.
            }

            val elementConfigClass = elementConfigFingerprint.originalClassDef
            val elementConfigClassType = elementConfigClass.type
            val elementConfigIndex = indexOfFirstInstructionReversedOrThrow(returnIndex) {
                val reference = getReference<MethodReference>()
                reference?.definingClass == elementConfigClassType
            }
            val elementConfigStringBuilderField = elementConfigClass.fields.single { field ->
                field.type == "Ljava/lang/StringBuilder;"
            }

            // Identifier is saved to a field just before the string builder.
            val putStringBuilderIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<FieldReference>()
                opcode == Opcode.IPUT_OBJECT &&
                        reference?.definingClass == elementConfigClassType &&
                        reference.type == "Ljava/lang/StringBuilder;"
            }
            val elementConfigIdentifierField = getInstruction<ReferenceInstruction>(
                indexOfFirstInstructionReversedOrThrow(putStringBuilderIndex) {
                    val reference = getReference<FieldReference>()
                    opcode == Opcode.IPUT_OBJECT &&
                            reference?.definingClass == elementConfigClassType &&
                            reference.type == "Ljava/lang/String;"
                }
            ).getReference<FieldReference>()

            // Could use some of these free registers multiple times, but this is inserting at a
            // return instruction so there is always multiple 4-bit registers available.
            val elementConfigRegister = getInstruction<FiveRegisterInstruction>(elementConfigIndex).registerC
            val identifierRegister = findFreeRegister(returnIndex, elementConfigRegister)
            val stringBuilderRegister = findFreeRegister(returnIndex, elementConfigRegister, identifierRegister)
            val thisRegister = findFreeRegister(returnIndex, elementConfigRegister, identifierRegister, stringBuilderRegister)
            val freeRegister = findFreeRegister(returnIndex, elementConfigRegister, identifierRegister, stringBuilderRegister, thisRegister)

            val invokeFilterInstructions = """
                iget-object v$identifierRegister, v$elementConfigRegister, $elementConfigIdentifierField
                iget-object v$stringBuilderRegister, v$elementConfigRegister, $elementConfigStringBuilderField
                invoke-static { v$identifierRegister, v$stringBuilderRegister }, $EXTENSION_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                move-result v$freeRegister
                move-object/from16 v$thisRegister, p0
                iput-boolean v$freeRegister, v$thisRegister, $lithoFilterResultField
            """

            if (is_19_18_or_greater) {
                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    invokeFilterInstructions
                )
            } else {
                val elementConfigMethod = conversionContextFingerprint.originalClassDef.methods
                    .single { method ->
                        !AccessFlags.STATIC.isSet(method.accessFlags) && method.returnType == elementConfigClassType
                    }

                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    """
                        # Element config is a method on a parameter.
                        move-object/from16 v$elementConfigRegister, p2 
                        invoke-virtual { v$elementConfigRegister }, $elementConfigMethod
                        move-result-object v$elementConfigRegister
                        
                        $invokeFilterInstructions

                        ${createReturnEmptyComponentInstructions(freeRegister)}
                    """
                )
            }
        }

        // endregion

        // region A/B test of new Litho native code.

        // Turn off native code that handles litho component names.  If this feature is on then nearly
        // all litho components have a null name and identifier/path filtering is completely broken.
        //
        // Flag was removed in 20.05. It appears a new flag might be used instead (45660109L),
        // but if the flag is forced on then litho filtering still works correctly.
        if (is_19_25_or_greater && !is_20_05_or_greater) {
            lithoComponentNameUpbFeatureFlagFingerprint.method.apply {
                // Don't use return early, so the debug patch logs if this was originally on.
                val insertIndex = indexOfFirstInstructionOrThrow(Opcode.RETURN)
                val register = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(insertIndex, "const/4 v$register, 0x0")
            }
        }

        // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
        // If this is enabled, then the litho protobuffer hook will always show an empty buffer
        // since it's no longer handled by the hooked Java code.
        lithoConverterBufferUpbFeatureFlagFingerprint.method.apply {
            val index = indexOfFirstInstructionOrThrow(Opcode.MOVE_RESULT)
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstruction(index + 1, "const/4 v$register, 0x0")
        }

        // endregion
    }

    finalize {
        lithoFilterFingerprint.method.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}
