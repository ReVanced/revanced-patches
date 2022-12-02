package app.revanced.patches.youtube.misc.fix.verticalscroll.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object CanScrollVerticallyFingerprint : MethodFingerprint(
    "Z",
    parameters = emptyList(),
    opcodes = listOf(Opcode.INSTANCE_OF),
    customFingerprint = { methodDef -> methodDef.definingClass.endsWith("SwipeRefreshLayout;") }
)