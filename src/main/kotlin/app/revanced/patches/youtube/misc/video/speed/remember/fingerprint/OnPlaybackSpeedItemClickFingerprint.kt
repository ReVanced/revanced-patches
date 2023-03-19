package app.revanced.patches.youtube.misc.video.speed.remember.fingerprint

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object OnPlaybackSpeedItemClickFingerprint : MethodFingerprint(
    customFingerprint = { it.name == "onItemClick" },
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL
    )
)