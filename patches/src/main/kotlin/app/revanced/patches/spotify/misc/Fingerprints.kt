package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint

internal val accountAttributeFingerprint = fingerprint {
    custom { _, c -> c.endsWith("internal/AccountAttribute;") }
}

internal val productStateProtoFingerprint = fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef ->
        classDef.endsWith("ProductStateProto;")
    }
}

internal val buildQueryParametersFingerprint = fingerprint {
    strings("trackRows", "device_type:tablet")
}

internal val contextMenuExperimentsFingerprint = fingerprint {
    parameters("L")
    strings("remove_ads_upsell_enabled")
}
