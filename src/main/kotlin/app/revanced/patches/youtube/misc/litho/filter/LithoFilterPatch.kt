package app.revanced.patches.youtube.misc.litho.filter

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.*
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import java.io.Closeable

@Patch(
    description = "Hooks the method which parses the bytes into a ComponentContext to filter components.",
    dependencies = [IntegrationsPatch::class]
)
@Suppress("unused")
object LithoFilterPatch : BytecodePatch(
    setOf(ComponentContextParserFingerprint, LithoFilterFingerprint, ProtobufBufferReferenceFingerprint)
), Closeable {
    private val MethodFingerprint.patternScanResult
        get() = result!!.scanResult.patternScanResult!!

    private val MethodFingerprint.patternScanEndIndex
        get() = patternScanResult.endIndex

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
        ComponentContextParserFingerprint.result?.also {
            arrayOf(
                EmptyComponentBuilderFingerprint,
                ReadComponentIdentifierFingerprint
            ).forEach { fingerprint ->
                if (fingerprint.resolve(context, it.mutableMethod, it.mutableClass)) return@forEach
                throw fingerprint.exception
            }
        }?.let { bytesToComponentContextMethod ->

            // region Pass the buffer into Integrations.

            ProtobufBufferReferenceFingerprint.result
                ?.mutableMethod?.addInstruction(0,
                    " invoke-static { p2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setProtoBuffer(Ljava/nio/ByteBuffer;)V")
                ?: throw ProtobufBufferReferenceFingerprint.exception

            // endregion

            // region Hook the method that parses bytes into a ComponentContext.

            val builderMethodIndex = EmptyComponentBuilderFingerprint.patternScanEndIndex
            val emptyComponentFieldIndex = builderMethodIndex + 2

            bytesToComponentContextMethod.mutableMethod.apply {
                val insertHookIndex = indexOfFirstInstruction {
                    opcode == Opcode.IPUT_OBJECT &&
                            getReference<FieldReference>()?.type == "Ljava/lang/StringBuilder;"
                } + 1
                if (insertHookIndex <= 0) throw PatchException("Could not find insert index")

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
                    getInstruction<OneRegisterInstruction>(ReadComponentIdentifierFingerprint.patternScanEndIndex).registerA

                // endregion

                // region Patch the method.

                // Insert the instructions that are responsible
                // to return an EmptyComponent instead of the original component if the filter method returns true.
                addInstructionsWithLabels(
                    insertHookIndex,
                    """
                        # Invoke the filter method.
                      
                        invoke-static { v$identifierRegister, v$stringBuilderRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->filter(Ljava/lang/String;Ljava/lang/StringBuilder;)Z
                        move-result v$free1
                       
                        if-eqz v$free1, :unfiltered

                        move-object/from16 v$free2, p1
                        invoke-static {v$free2}, $builderMethodDescriptor
                        move-result-object v$free2
                        iget-object v$free2, v$free2, $emptyComponentFieldDescriptor
                        return-object v$free2
                    """,
                    // Used to jump over the instruction which block the component from being created.
                    ExternalLabel("unfiltered", getInstruction(insertHookIndex))
                )
                // endregion
            }

            // endregion
        } ?: throw ComponentContextParserFingerprint.exception

        LithoFilterFingerprint.result?.mutableMethod?.apply {
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
        } ?: throw LithoFilterFingerprint.exception
    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.replaceInstruction(0, "const/16 v0, $filterCount")
}
