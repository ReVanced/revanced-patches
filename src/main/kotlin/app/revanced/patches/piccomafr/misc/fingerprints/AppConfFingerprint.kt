package app.revanced.patches.piccomafr.misc.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object AppConfFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.RETURN_OBJECT,
    ),
    strings = emptySet(),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "AppConf.kt"
    },
)
