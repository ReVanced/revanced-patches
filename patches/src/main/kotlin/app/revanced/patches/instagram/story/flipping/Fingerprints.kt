package app.revanced.patches.instagram.story.flipping

import app.revanced.patcher.fingerprint

internal val onStoryTimeoutActionFingerprint = fingerprint {
    returns("V")
    parameters("Ljava/lang/Object;")
    strings("userSession")
    custom { _, classDef ->
        classDef.type == "Linstagram/features/stories/fragment/ReelViewerFragment;"
    }
}
