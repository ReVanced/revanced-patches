package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ComponentContextParserFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.LithoFilterFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ProtobufBufferReferenceFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ReadComponentIdentifierFingerprint
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import java.io.Closeable

@Patch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
    dependencies = [IntegrationsPatch::class]
)
@Suppress("unused")
object LithoFilterPatch : BytecodePatch(
    setOf(
        ComponentContextParserFingerprint,
        LithoFilterFingerprint,
        ProtobufBufferReferenceFingerprint,
        ReadComponentIdentifierFingerprint
    )
), Closeable {
    private val Instruction.descriptor
        get() = (this as ReferenceInstruction).reference.toString()

    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/components/LithoFilterPatch;"

    internal lateinit var addFilter: (String) -> Unit
        private set

    private var filterCount = 0

    /**
     * The following patch inserts a hook into the method that parses the bytes into a ComponentContext.
     * This method contains a StringBuilder object that represents the pathBuilder of the component.
     * The pathBuilder is used to filter components by their path.
     *
     * Additionally, the method contains a reference to the components identifier.
     * The identifier is used to filter components by their identifier.
     *
     * The protobuf buffer is passed along from a different injection point before the filtering occurs.
     * The buffer is a large byte array that represents the component tree.
     * This byte array is searched for strings that indicate the current component.
     *
     * The following pseudo code shows how the patch works:
     *
     * class SomeOtherClass {
     *    // Called before ComponentContextParser.parseBytesToComponentContext method.
     *    public void someOtherMethod(ByteBuffer byteBuffer) {
     *        IntegrationsClass.setProtoBuffer(byteBuffer); // Inserted by this patch.
     *        ...
     *   }
     * }
     *
     * class ComponentContextParser {
     *
     *    public ComponentContext parseBytesToComponentContext(...) {
     *        ...
     *        if (IntegrationsClass.filter(identifier, pathBuilder)); // Inserted by this patch.
     *            return emptyComponent;
     *        ...
     *    }
     * }
     */
    override fun execute(context: BytecodeContext) {
        // region Hook the method that parses bytes into a ComponentContext.

        ComponentContextParserFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                // Get references that this patch needs.

                val builderMethodIndex = it.scanResult.patternScanResult!!.endIndex
                val emptyComponentFieldIndex = builderMethodIndex + 2

                val builderMethodDescriptor = getInstruction(builderMethodIndex).descriptor
                val emptyComponentFieldDescriptor = getInstruction(emptyComponentFieldIndex).descriptor

                // Get insert hook index and free register
                val stringIndex = it.scanResult.stringsScanResult!!.matches.first().index
                val insertHookIndex = stringIndex - 2 // This is fragile and could be improved.
                val register = getInstruction<OneRegisterInstruction>(stringIndex).registerA

                // Insert the instructions that are responsible
                // to return an EmptyComponent instead of the original component if the filterState method returns true.
                addInstructionsWithLabels(
                    insertHookIndex,
                    """
                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->filterState()Z
                        move-result v$register
                        if-eqz v$register, :unfiltered

                        move-object/from16 v$register, p1
                        invoke-static { v$register }, $builderMethodDescriptor
                        move-result-object v$register
                        iget-object v$register, v$register, $emptyComponentFieldDescriptor
                        return-object v$register
                    """,
                    // Used to jump over the instruction which block the component from being created..
                    ExternalLabel("unfiltered", getInstruction(insertHookIndex))
                )
            }
        }

        // endregion

        // region Pass the buffer into Integrations.

        ProtobufBufferReferenceFingerprint.resultOrThrow().mutableMethod.addInstruction(
            0,
            " invoke-static { p2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V"
        )

        // endregion

        // region Read component then store the result.

        ReadComponentIdentifierFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val identifierIndex = it.scanResult.patternScanResult!!.endIndex
                val identifierRegister = getInstruction<OneRegisterInstruction>(identifierIndex).registerA

                val insertHookIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IPUT_OBJECT &&
                            getReference<FieldReference>()?.type == "Ljava/lang/StringBuilder;"
                } + 1

                val stringBuilderRegister = getInstruction<FiveRegisterInstruction>(insertHookIndex - 2).registerC

                addInstruction(
                    insertHookIndex,
                    """
                        # Invoke the filter method.
                        invoke-static { v$identifierRegister, v$stringBuilderRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)V
                    """,
                )
            }
        }

        // endregion

        LithoFilterFingerprint.resultOrThrow().mutableMethod.apply {
            removeInstructions(2, 4) // Remove dummy filter.

            addFilter = { classDescriptor ->
                addInstructions(
                    2,
                    """
                        new-instance v1, $classDescriptor
                        invoke-direct {v1}, $classDescriptor-><init>()V
                        const/16 v2, ${filterCount++}
                        aput-object v1, v0, v2
                    """
                )
            }
        }
    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.replaceInstruction(0, "const/16 v0, $filterCount")
}
