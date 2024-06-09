package app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setRemoteConfigFingerprint = methodFingerprint {
    parameters("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
    strings("reddit_oauth_url")
}