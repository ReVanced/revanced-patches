package app.revanced.patches.instagram.patches.interaction.links.browser.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OpenLinksInExternalBrowserFingerprint : MethodFingerprint(
    strings = listOf("TrackingInfo.ARG_HIDE_SYSTEM_BAR", "TrackingInfo.ARG_MODULE_NAME"),
)
