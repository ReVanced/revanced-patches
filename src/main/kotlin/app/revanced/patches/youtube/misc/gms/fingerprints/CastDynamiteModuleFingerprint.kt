package app.revanced.patches.youtube.misc.gms.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CastDynamiteModuleFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
)