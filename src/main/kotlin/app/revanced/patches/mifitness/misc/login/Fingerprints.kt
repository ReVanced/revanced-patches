package app.revanced.patches.mifitness.misc.login

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val xiaomiAccountManagerConstructorFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;", "Z")
    custom { method, _ ->
        method.definingClass == "Lcom/xiaomi/passport/accountmanager/XiaomiAccountManager;"
    }
}
