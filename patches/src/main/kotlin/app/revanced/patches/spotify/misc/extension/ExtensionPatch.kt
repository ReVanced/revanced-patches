package app.revanced.patches.spotify.misc.extension

import app.revanced.patches.shared.misc.extension.sharedExtensionPatch

val sharedExtensionPatch = sharedExtensionPatch(
    "spotify", 
    mainActivityOnCreateHook,
    loadOrbitLibraryHook
)
