package app.revanced.patches.spotify.misc

import app.revanced.patcher.fingerprint

internal val accountAttributeFingerprint by fingerprint {
    custom { _, c -> c.endsWith("internal/AccountAttribute;") }
}

internal val productStateProtoFingerprint by fingerprint {
    returns("Ljava/util/Map;")
    custom { _, classDef ->
        classDef.endsWith("ProductStateProto;")
    }
}

internal val buildQueryParametersFingerprint by fingerprint {
    strings("trackRows", "device_type:tablet")
}
