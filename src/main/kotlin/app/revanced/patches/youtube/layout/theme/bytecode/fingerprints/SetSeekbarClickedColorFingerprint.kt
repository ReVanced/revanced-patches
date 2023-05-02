package app.revanced.patches.youtube.layout.theme.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object SetSeekbarClickedColorFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.CONST_HIGH16),
    strings = listOf("YOUTUBE", "PREROLL", "POSTROLL")
)