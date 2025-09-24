package app.revanced.patches.youtube.layout.hide.viewcount

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode 

internal val hideViewCountFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/CharSequence;")

    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.CONST_STRING,
        Opcode.RETURN_OBJECT,
    )
    strings(
        "Has attachmentRuns but drawableRequester is missing.",
    )
    
}
