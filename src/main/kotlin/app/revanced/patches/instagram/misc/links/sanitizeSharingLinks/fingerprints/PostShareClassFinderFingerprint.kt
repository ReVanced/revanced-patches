package app.revanced.patches.instagram.misc.links.sanitizeSharingLinks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object PostShareClassFinderFingerprint:MethodFingerprint (
    strings = listOf("media/%s/permalink")
)