package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Resolves to class found in [RollingNumberMeasureStaticLabelParentFingerprint].
 */
internal object RollingNumberMeasureStaticLabelFingerprint : MethodFingerprint(
    returnType = "F",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("Ljava/lang/String;"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.RETURN
    )
)