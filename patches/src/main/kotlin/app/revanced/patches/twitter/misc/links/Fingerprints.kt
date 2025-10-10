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

// Returns a shareable link string based on a tweet ID and a username.
internal val linkBuilderFingerprint = fingerprint {
    strings("/%1\$s/status/%2\$d")
}

internal val linkSharingDomainHelperFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getShareDomain" && classDef.type == EXTENSION_CLASS_DESCRIPTOR
    }
}
