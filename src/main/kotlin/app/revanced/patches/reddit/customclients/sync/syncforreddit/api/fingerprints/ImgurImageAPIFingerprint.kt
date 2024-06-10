package app.revanced.patches.reddit.customclients.sync.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val imgurImageAPIFingerprint = methodFingerprint {
    strings("https://imgur-apiv3.p.rapidapi.com/3/image")
}
