package app.revanced.patches.instagram.patches.ads.timeline.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object ShowAdFingerprint : MethodFingerprint(
    "Z",
    AccessFlags.PUBLIC or AccessFlags.STATIC or AccessFlags.FINAL,
    listOf("L", "L", "Z", "Z"),
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.IF_NEZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.RETURN,
    ),
)
