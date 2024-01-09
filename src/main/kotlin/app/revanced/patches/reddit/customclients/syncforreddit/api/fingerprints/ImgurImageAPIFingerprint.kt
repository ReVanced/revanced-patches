package app.revanced.patches.reddit.customclients.syncforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ImgurImageAPIFingerprint : MethodFingerprint(
    strings = listOf(
        "https://imgur-apiv3.p.rapidapi.com/3/image",
    )
)