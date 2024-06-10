package app.revanced.patches.reddit.customclients.boostforreddit.fix.slink.fingerprints
import app.revanced.patcher.fingerprint.methodFingerprint

internal val handleNavigationFingerprint = methodFingerprint {
    strings(
        "android.intent.action.SEARCH",
        "subscription",
        "sort",
        "period",
        "boostforreddit.com/themes",
    )
}
