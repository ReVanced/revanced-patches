package app.revanced.patches.instagram.feed

import app.revanced.patcher.fingerprint

internal val mainFeedRequestFingerprint = fingerprint {
    strings("Request{mReason=", ", mInstanceNumber=")
}
