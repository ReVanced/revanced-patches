package app.revanced.patches.instagram.hide.highlightsTray

import app.revanced.patcher.fingerprint

internal const val TARGET_STRING = "highlights_tray"

internal val highlightsUrlBuilderFingerprint = fingerprint {
    strings(TARGET_STRING,"X-IG-Accept-Hint")
}
