package app.revanced.patches.vsco.misc.pro

import app.revanced.patcher.fingerprint.methodFingerprint

internal val revCatSubscriptionFingerprint = methodFingerprint {
    returns("V")
    strings("use_debug_subscription_settings")
    custom { _, classDef ->
        classDef.endsWith("/RevCatSubscriptionSettingsRepository;")
    }
}
