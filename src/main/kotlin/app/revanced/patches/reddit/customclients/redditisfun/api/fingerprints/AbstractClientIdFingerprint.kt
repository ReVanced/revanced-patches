package app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

abstract class AbstractClientIdFingerprint(string: String) : MethodFingerprint(
    strings = listOfNotNull("yyOCBp.RHJhDKd", string),
)