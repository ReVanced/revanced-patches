package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getAuthorizationStringFingerprint = methodFingerprint {
    strings("authorize.compact?client_id")
}

internal val getBearerTokenFingerprint = methodFingerprint {
    strings("Basic")
}

internal val getUserAgentFingerprint = methodFingerprint {
    strings("android:com.laurencedawson.reddit_sync")
}

internal val imgurImageAPIFingerprint = methodFingerprint {
    strings("https://imgur-apiv3.p.rapidapi.com/3/image")
}
