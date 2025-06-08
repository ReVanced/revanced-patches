@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.is_19_25_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_05_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_22_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.shared.conversionContextFingerprintToString
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.insertLiteralOverride
import app.revanced.util.returnLate
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
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
                    """
                )
            }
        }

        // region Pass the buffer into extension.

        if (is_19_25_or_greater) {
            // Hook method that bridges between UPB buffer native code and FB Litho.
            protobufBufferReferenceFingerprint.let {
                // Hook the buffer after the call to jniDecode().
                it.method.addInstruction(
                    it.instructionMatches.last().index + 1,
                    "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer([B)V",
                )
            }
        }

        // Legacy Non native buffer.
        protobufBufferReferenceLegacyFingerprint.method.addInstruction(
            0,
            "invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
        )

        // endregion


        // region Modify the create component method and
        // if the component is filtered then return an empty component.

        // Find the identifier/path fields of the conversion context.
        val conversionContextIdentifierField = componentContextParserFingerprint.match().let {
            // Identifier field is loaded just before the string declaration.
            val index = it.method.indexOfFirstInstructionReversedOrThrow(
                it.instructionMatches.first().index
            ) {
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
                method -> AccessFlags.STATIC.isSet(method.accessFlags)
        }
        val emptyComponentField = classBy {
            // Only one field that matches.
            it.type == builderMethodDescriptor.returnType
        }.fields.single()

        componentCreateFingerprint.method.apply {
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
                    iget-object v$identifierRegister, v$freeRegister, $conversionContextIdentifierField
                    iget-object v$pathRegister, v$freeRegister, $conversionContextPathBuilderField
                    invoke-static { v$identifierRegister, v$pathRegister }, $EXTENSION_CLASS_DESCRIPTOR->shouldFilter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
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

        // endregion


        // region A/B test of new Litho native code.

        // Turn off native code that handles litho component names.  If this feature is on then nearly
        // all litho components have a null name and identifier/path filtering is completely broken.
        //
        // Flag was removed in 20.05. It appears a new flag might be used instead (45660109L),
        // but if the flag is forced on then litho filtering still works correctly.
        if (is_19_25_or_greater && !is_20_05_or_greater) {
            lithoComponentNameUpbFeatureFlagFingerprint.method.returnLate(false)
        }

        // Turn off a feature flag that enables native code of protobuf parsing (Upb protobuf).
        lithoConverterBufferUpbFeatureFlagFingerprint.let {
            // FIXME: Procool buffer has changed in 20.22, and UPB native code is now always enabled.
            if (is_20_22_or_greater) {
                Logger.getLogger(this::class.java.name).severe(
                    "Litho filtering does not yet support 20.22+  Many UI components will not be hidden.")
            }

            // 20.22 the flag is still enabled in one location, but what it does is not clear.
            // Disable it anyway.
            it.method.insertLiteralOverride(
                it.instructionMatches.first().index,
                false
            )
        }

        // endregion
    }

    finalize {
        lithoFilterFingerprint.method.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}