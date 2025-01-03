package app.revanced.patches.reddit.customclients.infinityforreddit.api

import app.revanced.patcher.fingerprint

internal val apiUtilsFingerprint by fingerprint {
    strings("native-lib")
}