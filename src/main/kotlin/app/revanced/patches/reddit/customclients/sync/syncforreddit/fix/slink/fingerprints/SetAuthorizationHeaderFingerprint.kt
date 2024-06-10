package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val setAuthorizationHeaderFingerprint = methodFingerprint {
    returns("Ljava/util/HashMap;")
    strings("Authorization", "bearer ")
    custom { methodDef, _ -> methodDef.definingClass == "Lcom/laurencedawson/reddit_sync/singleton/a;" }
}
