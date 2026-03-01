package app.revanced.patches.youtube.misc.check

import app.revanced.patches.shared.misc.checks.checkEnvironmentPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateMethod

internal val checkEnvironmentPatch = checkEnvironmentPatch(
    getMainActivityOnCreateMethod = { mainActivityOnCreateMethod },
    extensionPatch = sharedExtensionPatch,
    "com.google.android.youtube",
)
