package app.revanced.patches.twitter.misc.fxtwitter.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

object FxTwitterPatchFingerprint: MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
    opcodes = listOf(Opcode.RETURN_OBJECT),
    returnType = "Ljava/lang/String;",
    parameters = listOf("J", "Ljava/lang/String;"),
    strings = listOf("/%1\$s/status/%2\$d"),
)