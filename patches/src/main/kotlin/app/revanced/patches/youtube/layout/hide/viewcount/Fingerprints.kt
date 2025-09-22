package app.revanced.patches.youtube.layout.hide.viewcount

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val rnaKMethodFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/CharSequence;")
    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.INVOKE_STATIC,   // SpannableString.valueOf
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL_RANGE
    )
    custom { method, _ ->
        method.name == "k"
    }
}
