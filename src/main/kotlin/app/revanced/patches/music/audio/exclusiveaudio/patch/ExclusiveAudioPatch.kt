package app.revanced.patches.music.audio.exclusiveaudio.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.implementation.BytecodeData
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.patch.implementation.BytecodePatch
import app.revanced.patcher.patch.implementation.misc.PatchResult
import app.revanced.patcher.patch.implementation.misc.PatchResultError
import app.revanced.patcher.patch.implementation.misc.PatchResultSuccess
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patcher.smali.toInstruction
import app.revanced.patches.music.audio.exclusiveaudio.annotations.ExclusiveAudioCompatibility
import app.revanced.patches.music.audio.exclusiveaudio.signatures.ExclusiveAudioSignature
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Patch
@Name("exclusive-audio-playback")
@Description("Add the option to play music without video.")
@ExclusiveAudioCompatibility
@Version("0.0.1")
class ExclusiveAudioPatch : BytecodePatch(
    listOf(
        ExclusiveAudioSignature
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        val result = signatures.first().result!!.findParentMethod(@Name("audio-only-enabler-method") @MatchingMethod(
            "Lgmd;",
            "d"
        ) @DirectPatternScanMethod @ExclusiveAudioCompatibility @Version(
            "0.0.1"
        ) object : MethodSignature(
            "Z", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_INTERFACE,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT_OBJECT,
                Opcode.CHECK_CAST,
                Opcode.IF_NEZ,
                Opcode.IGET_OBJECT,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.GOTO,
                Opcode.INVOKE_VIRTUAL,
                Opcode.MOVE_RESULT,
                Opcode.RETURN
            )
        ) {}) ?: return PatchResultError("Required parent method could not be found.")

        val implementation = result.method.implementation!!
        implementation.replaceInstruction(
            implementation.instructions.count() - 1, "const/4 v0, 0x1".toInstruction()
        )
        implementation.addInstruction(
            "return v0".toInstruction()
        )

        return PatchResultSuccess()
    }
}