package app.revanced.patches.reddit.misc.tracking.url

import app.revanced.patcher.fingerprint

internal val shareLinkFormatterFingerprint = fingerprint {
    custom { _, classDef ->
        classDef.startsWith("Lcom/reddit/sharing/") && classDef.sourceFile == "UrlUtil.kt"
    }
}