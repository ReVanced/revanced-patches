package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke

internal val loadOrbitLibraryMethodMatch = firstMethodComposite {
    instructions(
        "orbit_library_load"(),
        "orbit-jni-spotify"()
    )
}
