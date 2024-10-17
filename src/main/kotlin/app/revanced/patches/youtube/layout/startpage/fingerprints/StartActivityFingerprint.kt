package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object StartActivityFingerprint : MethodFingerprint(
    parameters = listOf("Landroid/content/Intent;"),
)