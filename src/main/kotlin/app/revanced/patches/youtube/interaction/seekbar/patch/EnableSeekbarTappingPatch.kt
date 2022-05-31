package app.revanced.patches.youtube.interaction.seekbar.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.util.smali.toInstructions
import app.revanced.patches.youtube.interaction.seekbar.annotation.SeekbarTappingCompatibility
import app.revanced.patches.youtube.interaction.seekbar.signatures.SeekbarTappingParentSignature
import app.revanced.patches.youtube.interaction.seekbar.signatures.SeekbarTappingSignature
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.formats.Instruction11n
import org.jf.dexlib2.iface.instruction.formats.Instruction35c


@Patch(dependencies = [IntegrationsPatch::class])
@Name("seekbar-tapping")
@Description("Enable tapping on the seekbar of the YouTube player.")
@SeekbarTappingCompatibility
@Version("0.0.1")
class EnableSeekbarTappingPatch : BytecodePatch(
    listOf(
        SeekbarTappingParentSignature, SeekbarTappingSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
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

        // get the required register
        val instruction = implementation.instructions[result.scanResult.endIndex]
        if (instruction.opcode != Opcode.INVOKE_VIRTUAL) return PatchResultError("Could not find the correct register")
        val register = (instruction as Instruction35c).registerC

        // the instructions are written in reverse order.
        implementation.addInstructions(
            result.scanResult.endIndex + 1, """
               invoke-virtual { v$register, v2 }, ${oMethod.definingClass}->${oMethod.name}(I)V
               invoke-virtual { v$register, v2 }, ${pMethod.definingClass}->${pMethod.name}(I)V
            """.trimIndent().toInstructions()
        )

        // if tap-seeking is disabled, do not invoke the two methods above by jumping to the else label
        val elseLabel = implementation.newLabelForIndex(result.scanResult.endIndex + 1)
        implementation.addInstruction(
            result.scanResult.endIndex + 1, BuilderInstruction21t(Opcode.IF_EQZ, 0, elseLabel)
        )
        implementation.addInstructions(
            result.scanResult.endIndex + 1, """
                invoke-static { }, Lfi/razerman/youtube/preferences/BooleanPreferences;->isTapSeekingEnabled()Z
                move-result v0
            """.trimIndent().toInstructions()
        )
        return PatchResultSuccess()
    }
}