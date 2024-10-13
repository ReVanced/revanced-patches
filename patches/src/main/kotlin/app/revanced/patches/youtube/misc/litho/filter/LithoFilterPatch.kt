@file:Suppress("SpellCheckingInspection")

package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private val Fingerprint.patternMatch
    get() = match!!.patternMatch!!

private val Fingerprint.patternMatchEndIndex
    get() = patternMatch.endIndex

private val Instruction.descriptor
    get() = (this as ReferenceInstruction).reference.toString()

lateinit var addLithoFilter: (String) -> Unit
    private set

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/components/LithoFilterPatch;"

val lithoFilterPatch = bytecodePatch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
) {
    dependsOn(
        sharedExtensionPatch,
    )

    val componentContextParserMatch by componentContextParserFingerprint()
    val lithoFilterMatch by lithoFilterFingerprint()
    val protobufBufferReferenceMatch by protobufBufferReferenceFingerprint()

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
     * class ComponentContextParser {
     *
     *    public ComponentContext parseBytesToComponentContext(...) {
     *        ...
     *        if (extensionClass.filter(identifier, pathBuilder)); // Inserted by this patch.
     *            return emptyComponent;
     *        ...
     *    }
     * }
     */
    execute { context ->
        componentContextParserMatch.also {
            arrayOf(
                emptyComponentBuilderFingerprint,
                readComponentIdentifierFingerprint,
            ).forEach { fingerprint ->
                if (fingerprint.match(context, it.mutableMethod)) return@forEach
                throw fingerprint.exception
            }
        }.let { bytesToComponentContextMethod ->

            // region Pass the buffer into the extension.

            protobufBufferReferenceMatch.mutableMethod.addInstruction(
                0,
                " invoke-static { p2 }, $EXTENSION_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V",
            )

            // endregion

            // region Hook the method that parses bytes into a ComponentContext.

            val builderMethodIndex = emptyComponentBuilderFingerprint.patternMatchEndIndex
            val emptyComponentFieldIndex = builderMethodIndex + 2

            bytesToComponentContextMethod.mutableMethod.apply {
                val insertHookIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IPUT_OBJECT &&
                        getReference<FieldReference>()?.type == "Ljava/lang/StringBuilder;"
                } + 1

                // region Get free registers that this patch uses.
                // Registers are overwritten right after they are used in this patch, therefore free to clobber.

                val freeRegistersInstruction = getInstruction<FiveRegisterInstruction>(insertHookIndex - 2)

                // Later used to store the protobuf buffer object.
                val free1 = getInstruction<OneRegisterInstruction>(insertHookIndex).registerA
                // Later used to store the identifier of the component.
                // This register currently holds a reference to the StringBuilder object
                // that is required before clobbering.
                val free2 = freeRegistersInstruction.registerC

                @Suppress("UnnecessaryVariable")
                val stringBuilderRegister = free2

                // endregion

                // region Get references that this patch needs.

                val builderMethodDescriptor = getInstruction(builderMethodIndex).descriptor
                val emptyComponentFieldDescriptor = getInstruction(emptyComponentFieldIndex).descriptor

                val identifierRegister =
                    getInstruction<OneRegisterInstruction>(readComponentIdentifierFingerprint.patternMatchEndIndex).registerA

                // endregion

                // region Patch the method.

                // Insert the instructions that are responsible
                // to return an EmptyComponent instead of the original component if the filter method returns true.
                addInstructionsWithLabels(
                    insertHookIndex,
                    """
                        # Invoke the filter method.
                      
                        invoke-static { v$identifierRegister, v$stringBuilderRegister }, $EXTENSION_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                        move-result v$free1
                       
                        if-eqz v$free1, :unfiltered

                        move-object/from16 v$free2, p1
                        invoke-static {v$free2}, $builderMethodDescriptor
                        move-result-object v$free2
                        iget-object v$free2, v$free2, $emptyComponentFieldDescriptor
                        return-object v$free2
                    """,
                    // Used to jump over the instruction which block the component from being created.
                    ExternalLabel("unfiltered", getInstruction(insertHookIndex)),
                )
                // endregion
            }

            // endregion
        }

        lithoFilterMatch.mutableMethod.apply {
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
    }

    finalize {
        lithoFilterMatch.mutableMethod.replaceInstruction(0, "const/16 v0, $filterCount")
    }
}
