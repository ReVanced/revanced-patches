package app.revanced.patches.shared.misc.fix.verticalscroll.fingerprints


import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

object CanScrollVerticallyFingerprint : MethodFingerprint(
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
