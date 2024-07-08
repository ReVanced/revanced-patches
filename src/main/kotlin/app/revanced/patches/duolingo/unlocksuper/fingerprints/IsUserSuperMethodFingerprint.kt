package app.revanced.patches.duolingo.unlocksuper.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object IsUserSuperMethodFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/Object",
    parameters = listOf(
        "Ljava/lang/Object",
        "Ljava/lang/Object",
    ),
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    strings = listOf(
        "user",
        "heartsState",
        "superData",
    ),
    opcodes = listOf(Opcode.IGET_BOOLEAN),
)