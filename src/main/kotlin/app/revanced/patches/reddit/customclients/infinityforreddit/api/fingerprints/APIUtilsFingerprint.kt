package app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val apiUtilsFingerprint = methodFingerprint {
    strings("native-lib")
}