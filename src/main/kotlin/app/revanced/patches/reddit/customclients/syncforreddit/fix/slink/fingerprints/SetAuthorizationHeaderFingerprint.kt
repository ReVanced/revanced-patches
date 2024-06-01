package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SetAuthorizationHeaderFingerprint : MethodFingerprint(
    strings = listOf("Authorization", "bearer "),
    returnType = "Ljava/util/HashMap;",
    customFingerprint = { methodDef, _ -> methodDef.definingClass == "Lcom/laurencedawson/reddit_sync/singleton/a;" },
)
