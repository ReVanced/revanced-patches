package app.revanced.patches.reddit.customclients.infinityforreddit.subscription.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object SubscriptionActivityOnCreateFingerprint : MethodFingerprint(
    returnType = "V",
    accessFlags = AccessFlags.PUBLIC.value,
    parameters = listOf("Landroid/os/Bundle;"),
    customFingerprint = { method, classDef ->
        method.name == "onCreate" && classDef.type.endsWith("SubscriptionActivity;")
    }
)
