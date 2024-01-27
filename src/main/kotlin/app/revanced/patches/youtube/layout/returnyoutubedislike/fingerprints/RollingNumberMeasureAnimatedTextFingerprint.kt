package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object RollingNumberMeasureAnimatedTextFingerprint : MethodFingerprint(
    returnType = "Lj\$/util/Optional;",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    parameters = listOf(
        "L",
        "Ljava/lang/String;",
        "L"
    ),
    opcodes = listOf(
        Opcode.IGET, // First instruction of method
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.AGET,
        Opcode.CONST_4,
        Opcode.CONST_4, // Measured text width
    )
)