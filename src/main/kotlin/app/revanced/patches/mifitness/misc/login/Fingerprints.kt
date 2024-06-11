package app.revanced.patches.mifitness.misc.login

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val xiaomiAccountManagerConstructorFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.CONSTRUCTOR)
    parameters("Landroid/content/Context;", "Z")
    custom { _, classDef ->
        classDef.type == "Lcom/xiaomi/passport/accountmanager/XiaomiAccountManager;"
    }
}