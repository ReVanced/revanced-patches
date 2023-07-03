package app.revanced.patches.reddit.misc.tracking.url.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ShareLinkFormatterFingerprint : MethodFingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
    strings = listOf("uri.getQueryParameters(name)", "uri.queryParameterNames", "newUriBuilder.build().toString()"),
)