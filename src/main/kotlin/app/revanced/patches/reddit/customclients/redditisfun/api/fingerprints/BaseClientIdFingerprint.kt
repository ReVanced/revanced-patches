package app.revanced.patches.reddit.customclients.redditisfun.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

fun baseClientIdFingerprint(string: String) = methodFingerprint {
    strings("yyOCBp.RHJhDKd", string)
}