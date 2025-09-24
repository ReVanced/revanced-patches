package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.fingerprint

internal val permalinkResponseJsonParserFingerprint = fingerprint {
    strings("permalink", "PermalinkResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}
