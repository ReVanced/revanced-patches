package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.fingerprint

internal val startActivityFingerprint = fingerprint {
    parameters("Landroid/content/Intent;")
}
