package app.revanced.patches.twitter.misc.links.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val openLinkFingerprint = methodFingerprint {
    returns("V")
    parameters("Landroid/content/Context;", "Landroid/content/Intent;", "Landroid/os/Bundle;")
}
