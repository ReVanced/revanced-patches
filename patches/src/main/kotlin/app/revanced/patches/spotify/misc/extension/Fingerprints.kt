package app.revanced.patches.spotify.misc.extension

internal val BytecodePatchContext.loadOrbitLibraryMethod by gettingFirstMethodDeclaratively {
    strings("orbit_library_load", "orbit-jni-spotify")
}
