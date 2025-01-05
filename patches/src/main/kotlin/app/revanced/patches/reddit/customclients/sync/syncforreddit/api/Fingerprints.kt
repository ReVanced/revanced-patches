package app.revanced.patches.reddit.customclients.sync.syncforreddit.api

import app.revanced.patcher.fingerprint

internal val getAuthorizationStringFingerprint by fingerprint {
    strings("authorize.compact?client_id")
}

internal val getBearerTokenFingerprint by fingerprint {
    strings("Basic")
}

internal val getUserAgentFingerprint by fingerprint {
    strings("android:com.laurencedawson.reddit_sync")
}

internal val imgurImageAPIFingerprint by fingerprint {
    strings("https://imgur-apiv3.p.rapidapi.com/3/image")
}
