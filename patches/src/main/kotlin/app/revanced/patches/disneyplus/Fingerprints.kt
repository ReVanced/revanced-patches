package app.revanced.patches.disneyplus.ads

import app.revanced.patcher.fingerprint

internal val insertionGetPointsFingerprint = fingerprint {
    returns("Ljava/util/List")
    custom { method, _ ->
        method.name == "getPoints" &&
            method.definingClass == "Lcom/dss/sdk/internal/media/Insertion;"
    }
}

internal val insertionGetRangesFingerprint = fingerprint {
    returns("Ljava/util/List")
    custom { method, _ ->
        method.name == "getRanges" &&
            method.definingClass == "Lcom/dss/sdk/internal/media/Insertion;"
    }
}
