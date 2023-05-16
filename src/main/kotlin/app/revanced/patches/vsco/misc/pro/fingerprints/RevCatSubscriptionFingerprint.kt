package app.revanced.patches.vsco.misc.pro.fingerprints

import org.jf.dexlib2.AccessFlags
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RevCatSubscriptionFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("use_debug_subscription_settings"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/RevCatSubscriptionSettingsRepository;")
    }
)
