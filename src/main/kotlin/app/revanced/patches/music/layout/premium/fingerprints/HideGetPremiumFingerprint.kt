package app.revanced.patches.music.layout.premium.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object HideGetPremiumFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
        Opcode.IF_NEZ,
        Opcode.CONST_16,
        Opcode.GOTO,
        Opcode.NOP,
        Opcode.INVOKE_VIRTUAL
    )
)
