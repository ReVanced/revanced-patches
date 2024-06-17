package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.fingerprint

internal val openLinkFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/content/Context;", "Landroid/content/Intent;", "Landroid/os/Bundle;")
}

internal val sanitizeSharingLinksFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}
