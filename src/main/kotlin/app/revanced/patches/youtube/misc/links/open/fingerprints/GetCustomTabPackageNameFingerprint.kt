package app.revanced.patches.youtube.misc.links.open.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object GetCustomTabPackageNameFingerprint : MethodFingerprint(
    returnType = "L",
    accessFlags = AccessFlags.PUBLIC or AccessFlags.STATIC,
    opcodes = listOf(
        Opcode.CHECK_CAST,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.CONST_STRING
    ),
    strings = listOf("android.support.customtabs.action.CustomTabsService")
)