package app.revanced.patches.vsco.misc.pro.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val revCatSubscriptionFingerprint = methodFingerprint {
    returns("V")
    strings("use_debug_subscription_settings")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/RevCatSubscriptionSettingsRepository;")
    }
}
