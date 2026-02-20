package app.revanced.patches.tiktok.misc.share

import app.revanced.patcher.fingerprint

internal val shareUrlShorteningFingerprint = fingerprint {
    returns("LX/0rZz;")
    parameters(
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    )
    strings("item is null")
    custom { method, classDef ->
        classDef.type == "LX/0fTY;" && method.name == "LJIJI"
    }
}
