@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_05_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_20_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_22_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.shared.conversionContextFingerprintToString
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.util.logging.Logger

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
     *        // Checks if the component should be filtered.
     *        // Sets a thread local with the filtering result.
     *        extensionClass.filter(identifier, pathBuilder);  // Inserted by this patch.
     *
     *        ...
     *
     *        if (extensionClass.shouldFilter()) {  // Inserted by this patch.
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

        // region Pass the buffer into extension.

        if (is_20_22_or_greater) {
            protobufBufferReferenceFingerprint.method.addInstruction(
                0,
                "invoke-static { p3 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer([B)V",
            )
        } else {
            protobufBufferReferenceLegacyFingerprint.method.addInstruction(
                0,
                "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
            )
        }

        // endregion

        // region Hook the method that parses bytes into a ComponentContext.

        // 20.20+ has combined the two methods together,
        // and the sub parser fingerprint identifies the method to patch.
        val contextParserMethodToModifyFingerprint =
            if (is_20_20_or_greater) componentContextSubParserFingerprint
            else componentContextParserFingerprint

        // Allow the method to run to completion, and override the
        // return value with an empty component if it should be filtered.
        // It is important to allow the original code to always run to completion,
        // otherwise memory leaks and poor app performance can occur.
        //
        // The extension filtering result needs to be saved off somewhere, but cannot
        // save to a class field since the target class is called by multiple threads.
        // It would be great if there was a way to change the register count of the
        // method implementation and save the result to a high register to later use
        // in the method, but there is no simple way to do that.
        // Instead save the extension filter result to a thread local and check the
        // filtering result at each method return index.
        // String field for the litho identifier.
        contextParserMethodToModifyFingerprint.method.apply {
            val conversionContextClass = conversionContextFingerprintToString.originalClassDef

            val conversionContextIdentifierField = componentContextSubParserFingerprint.match(
                contextParserMethodToModifyFingerprint.originalClassDef
            ).let {
                // Identifier field is loaded just before the string declaration.
                val index = it.method.indexOfFirstInstructionReversedOrThrow(
                    it.instructionMatches.first().index
                ) {
                    val reference = getReference<FieldReference>()
                    reference?.definingClass == conversionContextClass.type
                            && reference.type == "Ljava/lang/String;"
                }
                it.method.getInstruction<ReferenceInstruction>(index).getReference<FieldReference>()
            }

            // StringBuilder field for the litho path.
            val conversionContextPathBuilderField = conversionContextClass.fields
                .single { field -> field.type == "Ljava/lang/StringBuilder;" }

            val conversionContextResultIndex = indexOfFirstInstructionOrThrow {
                val reference = getReference<MethodReference>()
                reference?.returnType == conversionContextClass.type
            } + 1

            val conversionContextResultRegister = getInstruction<OneRegisterInstruction>(
                conversionContextResultIndex
            ).registerA

            val identifierRegister = findFreeRegister(
                conversionContextResultIndex, conversionContextResultRegister
            )
            val stringBuilderRegister = findFreeRegister(
                conversionContextResultIndex, conversionContextResultRegister, identifierRegister
            )

            // Check if the component should be filtered, and save the result to a thread local.
            addInstructionsAtControlFlowLabel(
                conversionContextResultIndex + 1,
                """
                    iget-object v$identifierRegister, v$conversionContextResultRegister, $conversionContextIdentifierField
                    iget-object v$stringBuilderRegister, v$conversionContextResultRegister, $conversionContextPathBuilderField
                    invoke-static { v$identifierRegister, v$stringBuilderRegister }, $EXTENSION_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)V
                """
            )

            // Get the only static method in the class.
            val builderMethodDescriptor = emptyComponentFingerprint.classDef.methods.single {
                method -> AccessFlags.STATIC.isSet(method.accessFlags)
            }
            // Only one field.
            val emptyComponentField = classBy {
                it.type == builderMethodDescriptor.returnType
            }.fields.single()

            // Check at each return value if the component is filtered,
            // and return an empty component if filtering is needed.
            findInstructionIndicesReversedOrThrow(Opcode.RETURN_OBJECT).forEach { returnIndex ->
                val freeRegister = findFreeRegister(returnIndex)

                addInstructionsAtControlFlowLabel(
                    returnIndex,
                    """
                        invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->shouldFilter()Z
                        move-result v$freeRegister
                        if-eqz v$freeRegister, :unfiltered
        
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
        lithoConverterBufferUpbFeatureFlagFingerprint.let {
            it.method.apply {
                // 20.22+ flag is inverted.
                val override = if (is_20_22_or_greater) 0x1 else 0x0
                val index = indexOfFirstInstructionOrThrow(it.instructionMatches.first().index, Opcode.MOVE_RESULT)
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstruction(index + 1, "const/4 v$register, $override")
            }
        }

        // endregion
    }

    finalize {
        lithoFilterFingerprint.method.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}
