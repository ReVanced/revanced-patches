package app.revanced.patches.youtube.misc.litho.filter.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.annotation.LithoFilterCompatibility
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ComponentContextParserFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.EmptyComponentBuilderFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ProtobufBufferFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ReadComponentIdentifierFingerprint
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@DependsOn([IntegrationsPatch::class])
@Description("Hooks the method which parses the bytes into a ComponentContext to filter components.")
@LithoFilterCompatibility
@Version("0.0.1")
class LithoFilterPatch : BytecodePatch(
    listOf(ComponentContextParserFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ComponentContextParserFingerprint.result?.also {
            arrayOf(
                EmptyComponentBuilderFingerprint, ReadComponentIdentifierFingerprint, ProtobufBufferFingerprint
            ).forEach { fingerprint ->
                if (!fingerprint.resolve(context, it.mutableMethod, it.mutableClass))
                    return fingerprint.toErrorResult()
            }
        }?.let { result ->
            val builderMethodIndex = EmptyComponentBuilderFingerprint.patternScanEndIndex
            val emptyComponentFieldIndex = builderMethodIndex + 2

            result.mutableMethod.apply {
                val insertHookIndex = result.scanResult.patternScanResult!!.endIndex
                val builderMethodDescriptor = instruction(builderMethodIndex).descriptor
                val emptyComponentFieldDescriptor = instruction(emptyComponentFieldIndex).descriptor
                // Register is overwritten right after it is used in this patch, therefore free to clobber.
                val free = instruction<TwoRegisterInstruction>(insertHookIndex - 1).registerA
                val free2 = instruction<OneRegisterInstruction>(insertHookIndex).registerA

                @Suppress("UnnecessaryVariable")
                // The register, this patch clobbers, is previously used for the StringBuilder,
                // later on a new StringBuilder is instantiated on it.
                val stringBuilderRegister = free

                val identifierRegister =
                    instruction<OneRegisterInstruction>(ReadComponentIdentifierFingerprint.patternScanEndIndex).registerA

                // Parameter that holds a ref to a type with a field that ref the protobuf buffer object.
                val protobufParameterNumber = 3

                // Get the field that stores an protobuf buffer required below.
                val protobufBufferRefTypeRefFieldDescriptor =
                    instruction(ProtobufBufferFingerprint.patternScanStartIndex).descriptor
                val protobufBufferRefTypeDescriptor =
                    instruction(ProtobufBufferFingerprint.patternScanEndIndex - 1).descriptor
                val protobufBufferFieldDescriptor = "$protobufBufferRefTypeDescriptor->b:Ljava/nio/ByteBuffer;"

                addInstructions(
                    insertHookIndex, // right after setting the component.pathBuilder field.
                    """
                        # Get the protobuf buffer object.
                        
                        move-object/from16 v$free2, p$protobufParameterNumber
                        iget-object v$free2, v$free2, $protobufBufferRefTypeRefFieldDescriptor
                        check-cast v$free2, $protobufBufferRefTypeDescriptor
                      
                        # Register "free" now holds the protobuf buffer object
                       
                        iget-object v$free2, v$free2, $protobufBufferFieldDescriptor
                        
                        # Invoke the filter method.
                      
                        invoke-static { v$stringBuilderRegister, v$identifierRegister, v$free2 }, $FILTER_METHOD_DESCRIPTOR
                        move-result v$free
                       
                        if-eqz v$free, :not_an_ad
                       
                        # If the filter method returned true, then return a replacement empty component.
                        
                        move-object/from16 v$free, p1
                        invoke-static {v$free}, $builderMethodDescriptor
                        move-result-object v$free
                        iget-object v$free, v$free, $emptyComponentFieldDescriptor
                        return-object v$free
                    """,
                    listOf(ExternalLabel("not_an_ad", instruction(insertHookIndex)))
                )
            }
        } ?: return ComponentContextParserFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        private val MethodFingerprint.patternScanResult
            get() = result!!.scanResult.patternScanResult!!

        val MethodFingerprint.patternScanEndIndex
            get() = patternScanResult.endIndex

        val MethodFingerprint.patternScanStartIndex
            get() = patternScanResult.startIndex

        val Instruction.descriptor
            get() = (this as ReferenceInstruction).reference.toString()

        const val FILTER_METHOD_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/litho/LithoFilterPatch;" +
                    "->filter(Ljava/lang/StringBuilder;Ljava/lang/String;Ljava/nio/ByteBuffer;)Z"
    }
}