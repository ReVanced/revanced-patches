package app.revanced.patches.reddit.customclients.sync.syncforreddit.fix.slink.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val linkHelperOpenLinkFingerprint = methodFingerprint {
    strings("Link title: ")
}
