package app.revanced.patches.mifitness.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object MiFitnessLoginFingerprint : MethodFingerprint(
    accessFlags = AccessFlags.PRIVATE or AccessFlags.CONSTRUCTOR,
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass == "Lcom/xiaomi/passport/accountmanager/XiaomiAccountManager;"
    },
    parameters = listOf(
        "Landroid/content/Context;",
        "Z"
    )
)
