package app.revanced.patches.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val castDynamiteModuleFingerprint = methodFingerprint {
    strings("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
}
