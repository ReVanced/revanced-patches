package app.revanced.patches.pandora

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val disableAudioAdsPatch = bytecodePatch(
    name = "Disable audio ads",
) {
    compatibleWith("com.pandora.android")

    execute {
        constructUserDataFingerprint.method.apply {
            // First match is "hasAudioAds".
            val hasAudioAdsStringIndex = constructUserDataFingerprint.stringMatches!!.first().index
            val moveResultIndex = indexOfFirstInstructionOrThrow(hasAudioAdsStringIndex, Opcode.MOVE_RESULT)
            val hasAudioAdsRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

            addInstruction(
                moveResultIndex + 1,
                "const/4 v$hasAudioAdsRegister, 0"
            )
        }
    }
}
