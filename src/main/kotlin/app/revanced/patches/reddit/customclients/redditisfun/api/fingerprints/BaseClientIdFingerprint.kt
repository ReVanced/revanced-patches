package app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal abstract class BaseClientIdFingerprint(string: String) : MethodFingerprint(
    strings = listOfNotNull("yyOCBp.RHJhDKd", string),
)