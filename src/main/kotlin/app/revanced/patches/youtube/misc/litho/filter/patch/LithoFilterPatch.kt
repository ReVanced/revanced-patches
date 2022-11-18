package app.revanced.patches.youtube.misc.litho.filter.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.annotation.LithoFilterCompatibility
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ComponentContextParserFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.EmptyComponentBuilderFingerprint
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference

@DependsOn([IntegrationsPatch::class])
@Description("Hooks the method which parses the bytes into a ComponentContext to filter components.")
@LithoFilterCompatibility
@Version("0.0.1")
class LithoFilterPatch : BytecodePatch(
    listOf(ComponentContextParserFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ComponentContextParserFingerprint.result?.let { result ->
            val builderMethodIndex = EmptyComponentBuilderFingerprint
                .also { it.resolve(context, result.mutableMethod, result.mutableClass) }
                .let { it.result!!.scanResult.patternScanResult!!.startIndex }

            val emptyComponentFieldIndex = builderMethodIndex + 2

            with(result.mutableMethod) {
                val insertHookIndex = result.scanResult.patternScanResult!!.endIndex
                val clobberedRegister = (instruction(insertHookIndex - 3) as OneRegisterInstruction).registerA

                val builderMethodDescriptor = instruction(builderMethodIndex).toDescriptor()
                val emptyComponentFieldDescriptor = instruction(emptyComponentFieldIndex).toDescriptor()

                addInstructions(
                    insertHookIndex, // right after setting the component.pathBuilder field,
                    """
                        invoke-static {v5, v2}, Lapp/revanced/integrations/patches/LithoFilterPatch;->filter(Ljava/lang/StringBuilder;Ljava/lang/String;)Z
                        move-result v$clobberedRegister
                        if-eqz v$clobberedRegister, :not_an_ad
                        move-object/from16 v2, p1
                        invoke-static {v2}, $builderMethodDescriptor
                        move-result-object v0
                        iget-object v0, v0, $emptyComponentFieldDescriptor
                        return-object v0
                    """,
                    listOf(ExternalLabel("not_an_ad", instruction(insertHookIndex)))
                )
            }
        } ?: return PatchResultError("Could not find the method to hook.")

        return PatchResultSuccess()
    }

    private companion object {
        fun Instruction.toDescriptor() = when (val reference = (this as? ReferenceInstruction)?.reference) {
            is MethodReference -> "${reference.definingClass}->${reference.name}(${
                reference.parameterTypes.joinToString(
                    ""
                ) { it }
            })${reference.returnType}"
            is FieldReference -> "${reference.definingClass}->${reference.name}:${reference.type}"
            else -> throw PatchResultError("Unsupported reference type")
        }
    }
}
