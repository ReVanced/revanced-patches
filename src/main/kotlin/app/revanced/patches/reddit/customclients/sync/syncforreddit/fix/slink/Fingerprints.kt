package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink

import app.revanced.patcher.fingerprint.methodFingerprint

internal val linkHelperOpenLinkFingerprint = methodFingerprint {
    strings("Link title: ")
}

internal val setAuthorizationHeaderFingerprint = methodFingerprint {
    returns("Ljava/util/HashMap;")
    strings("Authorization", "bearer ")
    custom { methodDef, _ -> methodDef.definingClass == "Lcom/laurencedawson/reddit_sync/singleton/a;" }
}
