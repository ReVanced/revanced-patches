package app.revanced.patches.spotify.audio.bytecode.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.spotify.audio.annotation.DisableCaptureRestrictionCompatibility
import app.revanced.patches.spotify.audio.fingerprints.DisableCaptureRestrictionAudioDriverFingerprint
import app.revanced.patches.spotify.audio.resource.patch.DisableCaptureRestrictionResourcePatch
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@Name("disable-capture-restriction")
@DependsOn([DisableCaptureRestrictionResourcePatch::class])
@Description("Allows capturing Spotify's audio output while screen sharing or screen recording.")
@DisableCaptureRestrictionCompatibility
@Version("0.0.2")
class DisableCaptureRestrictionBytecodePatch : BytecodePatch(
    listOf(
        DisableCaptureRestrictionAudioDriverFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val method = DisableCaptureRestrictionAudioDriverFingerprint.result!!.mutableMethod

        var invokePosition: Int? = null
        var invokeParamRegister: Int? = null

        // Find INVOKE_VIRTUAL opcode with call to AudioAttributesBuilder.setAllowedCapturePolicy(I)
        for ((index, instruction) in method.implementation!!.instructions.withIndex()) {
            if(instruction.opcode != Opcode.INVOKE_VIRTUAL)
                continue

            val methodName = ((instruction as ReferenceInstruction).reference as MethodReference).name
            if (methodName != "setAllowedCapturePolicy")
                continue

            // Store register of the integer parameter for setAllowedCapturePolicy
            invokeParamRegister = (instruction as FiveRegisterInstruction).registerD
            invokePosition = index

            break
        }

        if(invokePosition == null || invokeParamRegister == null)
            return PatchResultError("Cannot find setAllowedCapturePolicy method call")

        // Walk back to the const/4 instruction that sets the parameter register
        var matchFound = false
        for (index in invokePosition downTo 0) {
            val instruction = method.instruction(index)
            if(instruction.opcode != Opcode.CONST_4)
                continue

            val register = (instruction as OneRegisterInstruction).registerA
            if(register != invokeParamRegister)
                continue

            // Replace parameter value
            method.replaceInstruction(
                index, "const/4 v$register, $ALLOW_CAPTURE_BY_ALL"
            )
            matchFound = true
            break
        }

        return if (matchFound)
            PatchResultSuccess()
        else
            PatchResultError("Const instruction not found")
    }

    private companion object {
        const val ALLOW_CAPTURE_BY_ALL = 0x01
    }
}