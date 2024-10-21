package app.revanced.patches.instagram.patches.interaction.links.tracking.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object PostShareClassFinderFingerprint : MethodFingerprint(
    strings = listOf("media/%s/permalink"),
)
