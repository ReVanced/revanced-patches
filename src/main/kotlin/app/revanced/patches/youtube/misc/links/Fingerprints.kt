package app.revanced.patches.youtube.misc.links

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal val abUriParserFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Ljava/lang/Object")
    parameters("Ljava/lang/Object")
    opcodes(
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT,
        Opcode.CHECK_CAST,
    )
    custom { method, classDef ->
        // This method is always called "a" because this kind of class always has a single (non synthetic) method.

        if (method.name != "a") return@custom false

        val count = classDef.methods.count()
        count == 2 || count == 3
    }
}

internal val httpUriParserFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Landroid/net/Uri")
    parameters("Ljava/lang/String")
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
    )
    strings("://")
}
