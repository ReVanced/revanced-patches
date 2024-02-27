package app.revanced.patches.music.utils.playertype

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.music.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.music.utils.playertype.fingerprint.PlayerTypeFingerprint
import app.revanced.util.exception

@Suppress("unused")
object PlayerTypeHookPatch : BytecodePatch(
    setOf(PlayerTypeFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        PlayerTypeFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstruction(
                    0,
                    "invoke-static {p1}, $INTEGRATIONS_CLASS_DESCRIPTOR->setPlayerType(Ljava/lang/Enum;)V"
                )
            }
        } ?: throw PlayerTypeFingerprint.exception

    }

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$UTILS_PATH/PlayerTypeHookPatch;"
}
