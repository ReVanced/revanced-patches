package app.revanced.patches.instagram.patches.ads.timeline.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object IsAdCheckOneFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf(),
    opcodes = listOf(
        Opcode.XOR_INT_LIT8,
        Opcode.IF_NE,
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
    ),
)
