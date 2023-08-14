package app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object GetUserAgentFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    AccessFlags.PUBLIC or AccessFlags.STATIC,
    emptyList(),
    listOf(
        Opcode.NEW_ARRAY,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.APUT_OBJECT,
        Opcode.CONST,
    )
)