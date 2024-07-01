package app.revanced.patches.soundcloud.offline.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.extensions.or
import com.android.tools.smali.dexlib2.Opcode

internal object OfflineSyncURLBuilderFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.FINAL,
    parameters = listOf("L","L"),
    opcodes = listOf(
        Opcode.IGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.FILLED_NEW_ARRAY
    ),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "DownloadOperations.kt"
    }
)