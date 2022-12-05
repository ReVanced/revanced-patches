package app.revanced.patches.twitch.ad.embedded.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CreateUsherClientFingerprint : MethodFingerprint(
    customFingerprint = { method ->
        method.definingClass.endsWith("Ltv/twitch/android/network/OkHttpClientFactory;") && method.name == "buildOkHttpClient"
    }
)