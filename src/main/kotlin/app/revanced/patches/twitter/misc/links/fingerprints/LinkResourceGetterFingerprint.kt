package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

// Gets Resource string for share link view available by pressing "Share via" button
object LinkResourceGetterFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
    opcodes = listOf(
        Opcode.CONST_STRING,
        Opcode.INVOKE_STATIC,
        Opcode.CONST_4,
        Opcode.NEW_ARRAY,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
    ),
    parameters = listOf("Landroid/content/res/Resources;"),
    strings = listOf("res.getString"),
)