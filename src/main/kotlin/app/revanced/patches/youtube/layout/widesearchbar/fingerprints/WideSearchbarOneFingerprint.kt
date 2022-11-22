package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object WideSearchbarOneFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L", "L"),
    listOf(Opcode.IF_NEZ, Opcode.SGET_OBJECT, Opcode.IGET_OBJECT, Opcode.INVOKE_STATIC)
)