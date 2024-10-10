package app.revanced.patches.youtube.misc.check

import app.revanced.patches.shared.misc.checks.checkEnvironmentPatch
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.shared.mainActivityOnCreateFingerprint

@Suppress("unused")
val checkEnvironmentPatch = checkEnvironmentPatch(
    mainActivityOnCreateFingerprint = mainActivityOnCreateFingerprint,
    extensionPatch = sharedExtensionPatch,
    "com.google.android.youtube",
)
