package app.revanced.patches.shared.misc.fix.verticalscroll.fingerprints


import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object CanScrollVerticallyFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    returnType = "Z",
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    ),
    customFingerprint = { methodDef, _ -> methodDef.definingClass.endsWith("SwipeRefreshLayout;") }
)
