package app.revanced.patches.strava.mediaupload

internal val BytecodePatchContext.getCompressionQualityMethod by gettingFirstMethodDeclaratively {
    custom { method, _ ->
        method.name == "getCompressionQuality"
    }
}

internal val BytecodePatchContext.getMaxDurationMethod by gettingFirstMethodDeclaratively {
    custom { method, _ ->
        method.name == "getMaxDuration"
    }
}

internal val BytecodePatchContext.getMaxSizeMethod by gettingFirstMethodDeclaratively {
    custom { method, _ ->
        method.name == "getMaxSize"
    }
}
