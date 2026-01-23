package app.revanced.patches.viber.ads

internal val BytecodePatchContext.findAdStringMethod by gettingFirstMethodDeclaratively {
    strings("viber_plus_debug_ads_free_flag")
}
