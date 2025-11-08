package app.revanced.patches.instagram.story.flipping

import app.revanced.patcher.fingerprint

internal val onStoryTimeoutActionFingerprint = fingerprint {
    custom { _, classDef ->
        classDef.type == "Linstagram/features/stories/fragment/ReelViewerFragment;"
    }
    strings("userSession")
    returns("V")
    parameters("Ljava/lang/Object;")
}
