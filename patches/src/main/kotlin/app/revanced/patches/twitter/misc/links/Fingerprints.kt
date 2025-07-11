package app.revanced.patches.twitter.misc.links

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val openLinkFingerprint by fingerprint {
    returns("V")
    parameters("Landroid/content/Context;", "Landroid/content/Intent;", "Landroid/os/Bundle;")
}

internal val sanitizeSharingLinksFingerprint by fingerprint {
    returns("Ljava/lang/String;")
    strings("<this>", "shareParam", "sessionToken")
}

// Returns a shareable link string based on a tweet ID and a username.
internal val linkBuilderFingerprint by fingerprint {
    strings("/%1\$s/status/%2\$d")
}

// Gets Resource string for share link view available by pressing "Share via" button.
internal val linkResourceGetterFingerprint by fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameters("Landroid/content/res/Resources;")
    literal { tweetShareLinkTemplateId }
}

internal val linkSharingDomainFingerprint by fingerprint {
    strings("https://fxtwitter.com")
}
