package app.revanced.patches.viber.misc.navbar
import app.revanced.patcher.fingerprint

internal val tabIdClassFingerprint = fingerprint {
    strings("shouldShowTabId")
}
