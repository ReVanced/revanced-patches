package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val loganSquareFingerprint = methodFingerprint {
    custom { _, classDef -> classDef.endsWith("LoganSquare;") }
}