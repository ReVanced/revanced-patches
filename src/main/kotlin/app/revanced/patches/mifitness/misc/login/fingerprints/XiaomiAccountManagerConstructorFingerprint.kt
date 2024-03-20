package app.revanced.patches.mifitness.misc.login.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object XiaomiAccountManagerConstructorFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/xiaomi/passport/accountmanager/XiaomiAccountManager;"
    },
    opcodes = listOf(Opcode.IF_NEZ),
)
