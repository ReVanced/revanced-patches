package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val xiaomiAccountManagerConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;", "Z")
    custom { method, _ ->
        method.definingClass == "Lcom/xiaomi/passport/accountmanager/XiaomiAccountManager;"
    }
}
