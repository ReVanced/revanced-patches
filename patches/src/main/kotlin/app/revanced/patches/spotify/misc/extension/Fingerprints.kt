package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.*

internal val loadOrbitLibraryMethodMatch = firstMethodComposite {
    instructions(
        "orbit_library_load"(),
        "orbit-jni-spotify"()
    )
}
