package app.revanced.patches.soundcloud.offline.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.extensions.or
import com.android.tools.smali.dexlib2.Opcode

internal object OfflineSyncHeaderVerificationFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L","L"),
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_STRING
    ),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "DownloadOperations.kt"
    }
)