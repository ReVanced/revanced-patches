package app.revanced.patches.twitch.ad.embedded

import app.revanced.patcher.fingerprint

internal val createsUsherClientFingerprint = fingerprint {
    custom { method, _ ->
        method.definingClass.endsWith("Ltv/twitch/android/network/OkHttpClientFactory;") && method.name == "buildOkHttpClient"
    }
}
