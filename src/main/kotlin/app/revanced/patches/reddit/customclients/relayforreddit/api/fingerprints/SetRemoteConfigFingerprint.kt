package app.revanced.patches.reddit.customclients.relayforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SetRemoteConfigFingerprint : MethodFingerprint(
    strings = listOf("reddit_oauth_url"),
    parameters = listOf("Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;")
)