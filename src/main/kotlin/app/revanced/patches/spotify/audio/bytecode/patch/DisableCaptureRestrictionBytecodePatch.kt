package app.revanced.patches.spotify.audio.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.spotify.audio.annotation.DisableCaptureRestrictionCompatibility
import app.revanced.patches.spotify.audio.fingerprints.DisableCaptureRestrictionAudioDriverFingerprint
import app.revanced.patches.spotify.audio.resource.patch.DisableCaptureRestrictionResourcePatch
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("disable-capture-restriction")
@DependsOn([DisableCaptureRestrictionResourcePatch::class])
@Description("Allows capturing Spotify's audio output while screen sharing or screen recording.")
@DisableCaptureRestrictionCompatibility
@Version("0.0.1")
class DisableCaptureRestrictionBytecodePatch : BytecodePatch(
    listOf(
        DisableCaptureRestrictionAudioDriverFingerprint
    )
) {
    private fun MutableMethod.replaceConstant4Instruction(index: Int, instruction: Instruction, with: Int) {
        val register = (instruction as OneRegisterInstruction).registerA
        this.replaceInstruction(
            index, "const/4 v$register, $with"
        )
    }

    override fun execute(context: BytecodeContext): PatchResult {
        val method = DisableCaptureRestrictionAudioDriverFingerprint.result!!.mutableMethod

        // Replace constant that contains the capture policy parameter for AudioAttributesBuilder.setAllowedCapturePolicy()
        val instruction = method.instruction(CONST_INSTRUCTION_POSITION)
        method.replaceConstant4Instruction(CONST_INSTRUCTION_POSITION, instruction, ALLOW_CAPTURE_BY_ALL)

        return PatchResultSuccess()
    }

    private companion object {
        const val CONST_INSTRUCTION_POSITION = 2
        const val ALLOW_CAPTURE_BY_ALL = 0x01
    }
}