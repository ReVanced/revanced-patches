package app.revanced.patches.reddit.misc.tracking.url.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object ShareLinkFormatterFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef ->
        classDef.startsWith("Lcom/reddit/sharing/") && classDef.sourceFile == "UrlUtil.kt"
    }
)