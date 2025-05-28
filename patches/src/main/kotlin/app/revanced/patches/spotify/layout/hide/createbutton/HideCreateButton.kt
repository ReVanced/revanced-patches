package app.revanced.patches.spotify.layout.hide.createbutton

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideCreateButtonPatch = bytecodePatch(
    name = "Hide Create button",
    description = "Hides the \"Create\" button from the navigation bar."
) {
    compatibleWith("com.spotify.music")

    execute {
        if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            // Create button does not exist in legacy versions.
            return@execute
        }

        playlistCreateButtonPositionExperimentFingerprint.method.apply {
            val invokeGetCreateButtonPositionIndex = indexOfFirstInstructionOrThrow(
                playlistCreateButtonPositionExperimentFingerprint.stringMatches!!.first().index,
                Opcode.INVOKE_VIRTUAL
            )
            // The default position is NOWHERE, which means hidden.
            val defaultPositionRegister = getInstruction<FiveRegisterInstruction>(invokeGetCreateButtonPositionIndex)
                .registerF
            val moveRegister = getInstruction<OneRegisterInstruction>(invokeGetCreateButtonPositionIndex + 1)
                .registerA

            replaceInstruction(
                invokeGetCreateButtonPositionIndex + 1,
                "move-object v$moveRegister, v$defaultPositionRegister"
            )
        }
    }
}
