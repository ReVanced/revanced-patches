package app.revanced.patches.youtube.misc.videobuffer.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object MaxBufferFingerprint : MethodFingerprint(
    opcodes = listOf(Opcode.SGET_OBJECT, Opcode.IGET, Opcode.IF_EQZ, Opcode.RETURN),
)