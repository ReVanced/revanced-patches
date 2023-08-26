package app.revanced.patches.duolingo.unlocksuper.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object IsUserSuperMethodFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Object",
    parameters = listOf("Ljava/lang/Object"),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf("user"),
    opcodes = listOf(Opcode.IGET_BOOLEAN),
)