package app.revanced.patches.youtube.misc.videobuffer.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object InvokeMaxBufferFingerprint : MethodFingerprint(
    "Z", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("J", "J", "F"),
    listOf(Opcode.CONST_WIDE_16),
    strings = listOf("scl.")
)