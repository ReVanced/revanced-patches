package app.revanced.patches.twitch.ad.embedded.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CreateUsherClientFingerprint : MethodFingerprint(
    customFingerprint = { method, _ ->
        method.definingClass.endsWith("Ltv/twitch/android/network/OkHttpClientFactory;") && method.name == "buildOkHttpClient"
    }
)