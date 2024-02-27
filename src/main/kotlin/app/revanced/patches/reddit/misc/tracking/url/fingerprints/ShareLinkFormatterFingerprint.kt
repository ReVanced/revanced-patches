package app.revanced.patches.reddit.misc.tracking.url.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object ShareLinkFormatterFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
    customFingerprint = { methodDef, classDef ->
        methodDef.definingClass.startsWith("Lcom/reddit/sharing/")
                && classDef.sourceFile == "UrlUtil.kt"
    }
)