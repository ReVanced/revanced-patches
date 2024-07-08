package app.revanced.patches.duolingo.unlocksuper.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object UserSerializationMethodFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR,
    strings = listOf(
        "betaStatus",
        "subscriberLevel",
    ),
    opcodes = listOf(
        Opcode.MOVE_FROM16,
        Opcode.IPUT_BOOLEAN,
    ),
)