package app.revanced.patches.youtube.layout.player.buttons.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object PlayerControlsVisibilityModelFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.INVOKE_DIRECT_RANGE),
    strings = listOf("hasNext", "hasPrevious", "Missing required properties:")
)