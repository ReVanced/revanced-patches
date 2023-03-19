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
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.annotation.LithoFilterCompatibility
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ComponentContextParserFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.EmptyComponentBuilderFingerprint
import app.revanced.patches.youtube.misc.litho.filter.fingerprints.ReadComponentIdentifierFingerprint
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction

@DependsOn([IntegrationsPatch::class])
@Description("Hooks the method which parses the bytes into a ComponentContext to filter components.")
@LithoFilterCompatibility
@Version("0.0.1")
class LithoFilterPatch : BytecodePatch(
    listOf(ComponentContextParserFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ComponentContextParserFingerprint.result?.also {
            arrayOf(EmptyComponentBuilderFingerprint, ReadComponentIdentifierFingerprint).forEach { fingerprint ->
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
                // Register is overwritten right after it is used for this patch, therefore free to clobber.
                val clobberedRegister = instruction(insertHookIndex).oneRegister

                @Suppress("UnnecessaryVariable")
                // The register, this patch clobbers, is previously used for the StringBuilder,
                // later on a new StringBuilder is instantiated on it.
                val stringBuilderRegister = clobberedRegister

                val identifierRegister = instruction(ReadComponentIdentifierFingerprint.patternScanEndIndex).oneRegister

                addInstructions(
                    insertHookIndex, // right after setting the component.pathBuilder field,
                    """
                        invoke-static {v$stringBuilderRegister, v$identifierRegister}, Lapp/revanced/integrations/patches/LithoFilterPatch;->filter(Ljava/lang/StringBuilder;Ljava/lang/String;)Z
                        move-result v$clobberedRegister
                        if-eqz v$clobberedRegister, :not_an_ad
                        move-object/from16 v$clobberedRegister, p1
                        invoke-static {v$clobberedRegister}, $builderMethodDescriptor
                        move-result-object v$clobberedRegister
                        iget-object v$clobberedRegister, v$clobberedRegister, $emptyComponentFieldDescriptor
                        return-object v$clobberedRegister
                    """,
                    listOf(ExternalLabel("not_an_ad", instruction(insertHookIndex)))
                )
            }
        } ?: return PatchResultError("Could not find the method to hook.")

        return PatchResultSuccess()
    }

    private companion object {
        val MethodFingerprint.patternScanEndIndex
            get() = result!!.scanResult.patternScanResult!!.endIndex

        val Instruction.descriptor
            get() = (this as ReferenceInstruction).reference.toString()

        val Instruction.oneRegister
            get() = (this as OneRegisterInstruction).registerA

    }
}