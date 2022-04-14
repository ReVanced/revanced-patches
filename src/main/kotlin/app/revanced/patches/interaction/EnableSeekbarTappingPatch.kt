package app.revanced.patches.interaction

import app.revanced.patcher.PatcherData
import app.revanced.patcher.extensions.AccessFlagExtensions.Companion.or
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchMetadata
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.signature.MethodMetadata
import app.revanced.patcher.signature.MethodSignature
import app.revanced.patcher.signature.MethodSignatureMetadata
import app.revanced.patcher.signature.PatternScanMethod
import app.revanced.patcher.smali.asInstructions
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.formats.Instruction11n

private val compatiblePackages = listOf("com.google.android.youtube")

class EnableSeekbarTappingPatch : Patch(
    metadata = PatchMetadata(
        "seekbar-tapping",
        "Enable seekbar tapping patch",
        "Enable tapping on the seekbar of the YouTube player.",
        compatiblePackages,
        "1.0.0"
    ),
    signatures = listOf(
        MethodSignature(
            methodSignatureMetadata = MethodSignatureMetadata(
                name = "enable-seekbar-tapping-parent-signature",
                methodMetadata = MethodMetadata(null, null), // unknown
                patternScanMethod = PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages = compatiblePackages,
                description = "Signature for a parent method, which is needed to find the actual method required to be patched.",
                version = "0.0.1"
            ),
            returnType = "L",
            accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
            methodParameters = listOf(),
            opcodes = listOf(
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_4,
                Opcode.NEW_ARRAY,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_WIDE,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_4,
                Opcode.APUT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_WIDE,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_STATIC,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CONST_4,
                Opcode.APUT_OBJECT,
                Opcode.CONST,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.RETURN_OBJECT
            )
        ),
        MethodSignature(
            methodSignatureMetadata = MethodSignatureMetadata(
                name = "enable-seekbar-tapping-signature",
                methodMetadata = MethodMetadata(null, null), // unknown
                patternScanMethod = PatternScanMethod.Fuzzy(2), // FIXME: Test this threshold and find the best value.
                compatiblePackages = compatiblePackages,
                description = "Signature for the method required to be patched.",
                version = "0.0.1"
            ),
            returnType = "Z",
            accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
            methodParameters = listOf("L"),
            opcodes = listOf(
                Opcode.CMPG_DOUBLE,
                Opcode.IF_GTZ,
                Opcode.GOTO,
                Opcode.INT_TO_FLOAT,
                Opcode.INT_TO_FLOAT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.IF_NEZ,
                Opcode.RETURN,
                Opcode.IGET_OBJECT,
                Opcode.IF_EQZ,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_WIDE,
                Opcode.INT_TO_FLOAT,
                Opcode.IGET,
                Opcode.IGET_OBJECT,
                Opcode.IGET,
                Opcode.DIV_INT_2ADDR,
                Opcode.ADD_INT,
                Opcode.SUB_INT_2ADDR,
                Opcode.INT_TO_FLOAT,
                Opcode.CMPG_FLOAT,
                Opcode.IF_GTZ,
                Opcode.INT_TO_FLOAT,
                Opcode.CMPG_FLOAT,
                Opcode.IF_GTZ,
                Opcode.CONST_4,
                Opcode.INVOKE_INTERFACE,
                Opcode.NEW_INSTANCE,
                Opcode.INVOKE_DIRECT,
                Opcode.IPUT_OBJECT,
                Opcode.INVOKE_VIRTUAL
            )
        )
    )
) {
    override fun execute(patcherData: PatcherData): PatchResult {
        var result = signatures.first().result!!

        val tapSeekMethods = mutableMapOf<String, Method>()

        // find the methods which tap the seekbar
        for (it in result.definingClassProxy.immutableClass.methods) {
            if (it.implementation == null) continue

            val instructions = it.implementation!!.instructions
            // here we make sure we actually find the method because it has more then 7 instructions
            if (instructions.count() < 7) continue

            // we know that the 7th instruction has the opcode CONST_4
            val instruction = instructions.elementAt(6)
            if (instruction.opcode != Opcode.CONST_4) continue

            // the literal for this instruction has to be either 1 or 2
            val literal = (instruction as Instruction11n).narrowLiteral

            // method founds
            if (literal == 1) tapSeekMethods["P"] = it
            if (literal == 2) tapSeekMethods["O"] = it
        }

        // replace map because we dont need the upper one anymore
        result = signatures.last().result!!

        val implementation = result.method.implementation!!

        // if tap-seeking is enabled, do not invoke the two methods below
        val pMethod = tapSeekMethods["P"]!!
        val oMethod = tapSeekMethods["O"]!!

        implementation.addInstructions(
            result.scanData.endIndex,
            """
               invoke-virtual { v12, v2 }, ${oMethod.definingClass}->${oMethod.name}(I)V
               invoke-virtual { v12, v2 }, ${pMethod.definingClass}->${pMethod.name}(I)V
            """.trimIndent().asInstructions()
        )

        // if tap-seeking is disabled, do not invoke the two methods above by jumping to the else label
        val elseLabel = implementation.newLabelForIndex(result.scanData.endIndex)
        implementation.addInstruction(
            result.scanData.endIndex,
            BuilderInstruction21t(Opcode.IF_EQZ, 0, elseLabel)
        )
        implementation.addInstructions(
            result.scanData.endIndex,
            """
                invoke-static { }, Lfi/razerman/youtube/preferences/BooleanPreferences;->isTapSeekingEnabled()Z
                move-result v0
            """.trimIndent().asInstructions()
        )
        return PatchResultSuccess()
    }
}