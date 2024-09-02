package app.revanced.patches.instagram.patches.links.sanitizeSharingLinks.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object PostShareClassFinderFingerprint : MethodFingerprint(
    strings = listOf("media/%s/permalink"),
)
