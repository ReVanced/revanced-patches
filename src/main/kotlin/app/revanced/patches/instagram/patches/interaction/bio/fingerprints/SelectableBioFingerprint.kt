package app.revanced.patches.instagram.patches.interaction.bio.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SelectableBioFingerprint : MethodFingerprint(
    strings = listOf("is_bio_visible"),
    returnType = "V",
)
