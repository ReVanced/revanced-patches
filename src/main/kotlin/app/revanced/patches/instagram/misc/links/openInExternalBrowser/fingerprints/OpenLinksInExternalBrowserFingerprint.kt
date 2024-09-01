package app.revanced.patches.instagram.misc.links.openInExternalBrowser.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object OpenLinksInExternalBrowserFingerprint:MethodFingerprint (
    strings = listOf("TrackingInfo.ARG_HIDE_SYSTEM_BAR","TrackingInfo.ARG_MODULE_NAME"),

)