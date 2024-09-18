package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object IntentActionFingerprint : MethodFingerprint(
    parameters = listOf("Landroid/content/Intent;"),
    strings = listOf("has_handled_intent"),
)