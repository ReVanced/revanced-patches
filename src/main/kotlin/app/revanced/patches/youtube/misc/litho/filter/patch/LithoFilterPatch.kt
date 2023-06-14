package app.revanced.patches.youtube.misc.litho.filter.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.*
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import java.io.Closeable

@DependsOn([IntegrationsPatch::class])
@Description("Hooks the method which parses the bytes into a ComponentContext to filter components.")
@Version("0.0.1")
class LithoFilterPatch : BytecodePatch(
    listOf(ComponentContextParserFingerprint, LithoFilterFingerprint)
), Closeable {
    override fun execute(context: BytecodeContext): PatchResult {
        ComponentContextParserFingerprint.result?.also {
            arrayOf(
                EmptyComponentBuilderFingerprint,
                ReadComponentIdentifierFingerprint,
                ProtobufBufferFingerprint
            ).forEach { fingerprint ->
                if (fingerprint.resolve(context, it.mutableMethod, it.mutableClass)) return@forEach
                return fingerprint.toErrorResult()
            }
        }?.let { result ->
            val builderMethodIndex = EmptyComponentBuilderFingerprint.patternScanEndIndex
            val emptyComponentFieldIndex = builderMethodIndex + 2

            result.mutableMethod.apply {
                val insertHookIndex = result.scanResult.patternScanResult!!.endIndex

                // region Get free registers that this patch uses
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

                // region Get references that this patch needs

                val builderMethodDescriptor = getInstruction(builderMethodIndex).descriptor
                val emptyComponentFieldDescriptor = getInstruction(emptyComponentFieldIndex).descriptor

                val identifierRegister =
                    getInstruction<OneRegisterInstruction>(ReadComponentIdentifierFingerprint.patternScanEndIndex).registerA

                // Parameter that holds a ref to a type with a field that ref the protobuf buffer object.
                val protobufParameterNumber = 3

                // Get the field that stores an protobuf buffer required below.
                val protobufBufferRefTypeRefFieldDescriptor =
                    getInstruction(ProtobufBufferFingerprint.patternScanStartIndex).descriptor
                val protobufBufferRefTypeDescriptor =
                    getInstruction(ProtobufBufferFingerprint.patternScanEndIndex - 1).descriptor
                val protobufBufferFieldDescriptor = "$protobufBufferRefTypeDescriptor->b:Ljava/nio/ByteBuffer;"

                // endregion

                // region Patch the method

                // Insert the instructions that are responsible
                // to return an EmptyComponent instead of the original component if the filter method returns false.
                addInstructionsWithLabels(
                    insertHookIndex,
                    """
                        # Get the protobuf buffer object.
                        
                        move-object/from16 v$free1, p$protobufParameterNumber
                        iget-object v$free1, v$free1, $protobufBufferRefTypeRefFieldDescriptor
                        check-cast v$free1, $protobufBufferRefTypeDescriptor
                      
                        # Register "free" now holds the protobuf buffer object
                       
                        iget-object v$free1, v$free1, $protobufBufferFieldDescriptor

                        # Invoke the filter method.
                      
                        invoke-static { v$stringBuilderRegister, v$identifierRegister, v$free1 }, $FILTER_METHOD_DESCRIPTOR
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
        } ?: return ComponentContextParserFingerprint.toErrorResult()

        LithoFilterFingerprint.result?.mutableMethod?.apply {
            removeInstructions(2, 4) // Remove dummy filter.

            addFilter = { classDescriptor ->
                addInstructions(
                    2,
                    """
                        new-instance v1, $classDescriptor
                        invoke-direct {v1}, $classDescriptor-><init>()V
                        const/4 v2, ${filterCount++}
                        aput-object v1, v0, v2
                    """
                )
            }
        } ?: return LithoFilterFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.replaceInstruction(0, "const/4 v0, $filterCount")

    companion object {
        private val MethodFingerprint.patternScanResult
            get() = result!!.scanResult.patternScanResult!!

        private val MethodFingerprint.patternScanEndIndex
            get() = patternScanResult.endIndex

        private val MethodFingerprint.patternScanStartIndex
            get() = patternScanResult.startIndex

        private val Instruction.descriptor
            get() = (this as ReferenceInstruction).reference.toString()

        private const val FILTER_METHOD_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/components/LithoFilterPatch;" +
                    "->filter(Ljava/lang/StringBuilder;Ljava/lang/String;Ljava/nio/ByteBuffer;)Z"

        internal lateinit var addFilter: (String) -> Unit
            private set

        private var filterCount = 0
    }
}