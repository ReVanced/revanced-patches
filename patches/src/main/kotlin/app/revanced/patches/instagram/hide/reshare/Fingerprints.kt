package app.revanced.patches.instagram.hide.reshare

import app.revanced.patcher.fingerprint

internal val mediaJsonParserFingerprint = fingerprint {
    custom { method, classDef ->classDef.type == "LX/5rs;" && method.name == "A01"}
}

internal val mediaJsonParserFingerprint3 = fingerprint {
    custom { method, classDef ->classDef.type == "LX/7Sz;" && method.name == "A04"}
}
