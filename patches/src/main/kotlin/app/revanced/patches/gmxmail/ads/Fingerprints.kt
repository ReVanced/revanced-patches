package app.revanced.patches.gmxmail.ads

import app.revanced.patcher.fingerprint

internal val getAdvertisementStatusFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "getAdvertisementStatus" && classDef.endsWith("/PayMailManager;")
    }
}

internal val isUpsellingPossibleFingerprint = fingerprint {
    custom { method, classDef ->
        method.name == "isUpsellingPossible" && classDef.endsWith("/PayMailManager;")
    }
}