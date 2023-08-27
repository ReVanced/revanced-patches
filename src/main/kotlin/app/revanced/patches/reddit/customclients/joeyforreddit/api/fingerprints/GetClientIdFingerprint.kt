package app.revanced.patches.reddit.customclients.joeyforreddit.api.fingerprints

import app.revanced.patcher.extensions.or
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetClientIdFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    opcodes = listOf(
        Opcode.CONST,               // R.string.valuable_cid
        Opcode.INVOKE_STATIC,       // StringMaster.decrypt
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    ),
    customFingerprint = custom@{ _, classDef ->
        classDef.sourceFile == "AuthUtility.java"
    }
)