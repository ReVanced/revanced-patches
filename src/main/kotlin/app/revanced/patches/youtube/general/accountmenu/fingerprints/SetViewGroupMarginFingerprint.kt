package app.revanced.patches.youtube.general.accountmenu.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object SetViewGroupMarginFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    parameters = listOf("Z"),
    opcodes = listOf(
        Opcode.INVOKE_STATIC,
        Opcode.IGET_OBJECT,
        Opcode.CONST_16,
        Opcode.INVOKE_VIRTUAL
    )
)