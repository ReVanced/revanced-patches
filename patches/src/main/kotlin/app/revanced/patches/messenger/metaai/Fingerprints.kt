package app.revanced.patches.messenger.metaai

import app.revanced.patcher.fingerprint

internal val getMobileConfigBoolFingerprint = fingerprint {
    parameters("J")
    returns("Z")
    custom { method, classDef ->
        method.implementation ?: return@custom false  // unsure if this is necessary
        classDef.interfaces.contains("Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;")
    }
}