package app.revanced.patches.piccomafr.misc.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object OpenToUserFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.STATIC or AccessFlags.CONSTRUCTOR,
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.SPUT_OBJECT,
        Opcode.RETURN_VOID,
    ),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "BuildConfig.java"
    },
)
