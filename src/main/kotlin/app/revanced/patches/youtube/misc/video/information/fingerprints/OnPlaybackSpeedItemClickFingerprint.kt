package app.revanced.patches.youtube.misc.video.information.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object OnPlaybackSpeedItemClickFingerprint : MethodFingerprint(
    customFingerprint = { it.name == "onItemClick" },
    opcodes = listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID
    )
)