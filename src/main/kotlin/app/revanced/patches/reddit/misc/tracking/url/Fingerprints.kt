package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.fingerprint.methodFingerprint

internal val shareLinkFormatterFingerprint = methodFingerprint {
    custom { _, classDef ->
        classDef.startsWith("Lcom/reddit/sharing/") && classDef.sourceFile == "UrlUtil.kt"
    }
}