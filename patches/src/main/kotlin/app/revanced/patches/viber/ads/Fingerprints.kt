package app.revanced.patches.viber.ads

import app.revanced.patcher.fingerprint

const val ADS_FREE_STR = "viber_plus_debug_ads_free_flag"

internal val findAdStringFingerprint = fingerprint {
    strings(ADS_FREE_STR)
}
