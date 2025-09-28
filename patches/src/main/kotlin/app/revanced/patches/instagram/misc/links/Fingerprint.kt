package app.revanced.patches.instagram.misc.links
import app.revanced.patcher.fingerprint

internal val inAppBrowserFunctionFingerprint = fingerprint {
    returns("Z")
    strings("TrackingInfo.ARG_MODULE_NAME", TARGET_STRING)
}
