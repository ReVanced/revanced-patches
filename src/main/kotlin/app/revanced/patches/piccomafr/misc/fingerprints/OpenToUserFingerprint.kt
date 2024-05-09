package app.revanced.patches.piccomafr.misc.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.extensions.or

internal object OpenToUserFingerprint : MethodFingerprint(
    strings = emptySet(),
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.SPUT_OBJECT,
        Opcode.RETURN_VOID
    ),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "BuildConfig.java"
    },
    accessFlags = AccessFlags.STATIC or AccessFlags.CONSTRUCTOR
)
