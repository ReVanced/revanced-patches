package app.revanced.patches.viber.ads

import app.revanced.patcher.fingerprint

internal val findAdStringFingerprint = fingerprint {
    strings("viber_plus_debug_ads_free_flag")
}
