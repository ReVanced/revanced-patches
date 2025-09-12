package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink

import app.revanced.patcher.fingerprint

internal val linkHelperOpenLinkFingerprint by fingerprint {
    strings("Link title: ")
}

internal val setAuthorizationHeaderFingerprint by fingerprint {
    returns("Ljava/util/HashMap;")
    strings("Authorization", "bearer ")
    custom { method, _ -> method.definingClass == "Lcom/laurencedawson/reddit_sync/singleton/a;" }
}
