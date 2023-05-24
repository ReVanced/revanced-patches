package app.revanced.patches.vsco.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RevCatSubscriptionFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("use_debug_subscription_settings"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/RevCatSubscriptionSettingsRepository;")
    }
)