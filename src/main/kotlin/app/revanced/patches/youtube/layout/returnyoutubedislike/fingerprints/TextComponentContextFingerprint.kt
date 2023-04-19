package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

/**
 * Resolves against the same method that [TextComponentContextFingerprint] resolves to.
 */
object TextComponentContextFingerprint : MethodFingerprint(
    returnType = "L",
    access = AccessFlags.PROTECTED or AccessFlags.FINAL,
    parameters = listOf("L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT, // conversion context field name
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IGET,
        Opcode.IGET,
        Opcode.IGET,
    )
)