package app.revanced.patches.spotify.misc.check

import app.revanced.patches.shared.misc.checks.checkEnvironmentPatch
import app.revanced.patches.spotify.shared.mainActivityOnCreateFingerprint
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch

internal val checkEnvironmentPatch = checkEnvironmentPatch(
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    "com.spotify.music",
)
