package app.revanced.patches.shared.misc.fix.verticalscroll

import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint

internal val canScrollVerticallyFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    opcodes(
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT
    )
    custom { _, classDef -> classDef.endsWith("SwipeRefreshLayout;") }
}