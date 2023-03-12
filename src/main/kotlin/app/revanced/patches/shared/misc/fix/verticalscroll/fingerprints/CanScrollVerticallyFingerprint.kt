package app.revanced.patches.shared.misc.fix.verticalscroll.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object CanScrollVerticallyFingerprint : MethodFingerprint(
    "Z",
    parameters = emptyList(),
    opcodes = listOf(
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
    ),
    customFingerprint = { methodDef -> methodDef.definingClass.endsWith("SwipeRefreshLayout;") }
)
