package app.revanced.patches.music.utils.microg.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CastDynamiteModuleFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
)