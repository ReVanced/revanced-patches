package app.revanced.patches.reddit.customclients.infinityforreddit.api.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object APIUtilsFingerprint : MethodFingerprint(
    strings = listOf("native-lib")
)