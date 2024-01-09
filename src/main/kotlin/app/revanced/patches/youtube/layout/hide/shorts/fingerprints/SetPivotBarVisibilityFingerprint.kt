package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SetPivotBarVisibilityFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    parameters = listOf("Z"),
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.IF_EQZ
    )
)