package app.revanced.patches.piccomafr.misc.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.extensions.or

internal object AppConfFingerprint : MethodFingerprint(
    strings = emptySet(),
    customFingerprint = { _, classDef ->
        classDef.sourceFile == "AppConf.kt"
    },
    accessFlags = AccessFlags.PRIVATE or AccessFlags.FINAL,
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.RETURN_OBJECT
    )

)
