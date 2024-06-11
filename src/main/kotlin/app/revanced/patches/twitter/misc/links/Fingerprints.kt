package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.fingerprint.methodFingerprint

internal val openLinkFingerprint = methodFingerprint {
    returns("V")
    parameters("Landroid/content/Context;", "Landroid/content/Intent;", "Landroid/os/Bundle;")
}

internal val sanitizeSharingLinksFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}
