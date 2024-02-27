package app.revanced.patches.youtube.misc.codec.video.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object VideoSecondaryFingerprint : MethodFingerprint(
    returnType = "Z",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf("L", "I"),
    opcodes = listOf(
        Opcode.RETURN,
        Opcode.CONST_4,
        Opcode.RETURN
    )
)
