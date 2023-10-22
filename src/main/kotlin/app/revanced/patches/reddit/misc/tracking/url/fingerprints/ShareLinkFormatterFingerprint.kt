package app.revanced.patches.reddit.misc.tracking.url.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object ShareLinkFormatterFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.definingClass.startsWith("Lcom/reddit/sharing/") && classDef.sourceFile == "UrlUtil.kt"
    }
)