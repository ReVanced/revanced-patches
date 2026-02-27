package app.revanced.patches.spotify.misc.check

import app.revanced.patches.shared.misc.checks.checkEnvironmentPatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.patches.spotify.shared.mainActivityOnCreateMethod

internal val checkEnvironmentPatch = checkEnvironmentPatch(
    getMainActivityOnCreateMethod = { mainActivityOnCreateMethod },
    extensionPatch = sharedExtensionPatch,
    "com.spotify.music",
)
