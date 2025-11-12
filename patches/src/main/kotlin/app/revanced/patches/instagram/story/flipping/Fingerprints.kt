package app.revanced.patches.instagram.story.flipping

import app.revanced.patcher.fingerprint

internal val onStoryTimeoutActionFingerprint = fingerprint {
    parameters("Ljava/lang/Object;")
    returns("V")
    strings("userSession")
    custom { _, classDef ->
        classDef.type == "Linstagram/features/stories/fragment/ReelViewerFragment;"
    }
}
