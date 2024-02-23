package app.revanced.patches.reddit.customclients.syncforreddit.fix.slink.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object LinkHelperOpenLinkFingerprint: MethodFingerprint(
    strings = listOf("Link title: ")
)
