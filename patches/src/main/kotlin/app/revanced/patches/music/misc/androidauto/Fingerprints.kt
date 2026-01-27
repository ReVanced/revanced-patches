package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.fingerprint

internal val checkCertificateFingerprint = fingerprint {
    returns("Z")
    parameters("Ljava/lang/String;")
    strings(
        "X509",
        "Failed to get certificate" // Partial String match.
    )
}

internal val searchMediaItemsConstructorFingerprint = fingerprint {
    returns("V")
    strings("ytm_media_browser/search_media_items")
}

internal val searchMediaItemsExecuteFingerprint = fingerprint {
    parameters()
}