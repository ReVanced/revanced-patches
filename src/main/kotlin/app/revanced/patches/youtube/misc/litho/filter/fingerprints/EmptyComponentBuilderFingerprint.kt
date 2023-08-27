package app.revanced.patches.youtube.misc.litho.filter.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object EmptyComponentBuilderFingerprint : MethodFingerprint(
    opcodes = listOf(
        Opcode.INVOKE_INTERFACE,
        Opcode.INVOKE_STATIC_RANGE
    ),
)