package app.revanced.patches.youtube.layout.startpage.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val startActivityFingerprint = methodFingerprint {
    parameters("Landroid/content/Intent;")
}
