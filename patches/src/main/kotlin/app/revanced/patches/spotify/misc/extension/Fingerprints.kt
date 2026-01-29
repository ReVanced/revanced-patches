package app.revanced.patches.spotify.misc.extension

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.loadOrbitLibraryMethodMatch by composingFirstMethod {
    instructions(
        "orbit_library_load"(),
        "orbit-jni-spotify"(),
    )
}
