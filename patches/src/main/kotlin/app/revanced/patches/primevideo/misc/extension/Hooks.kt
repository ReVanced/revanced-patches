package app.revanced.patches.primevideo.misc.extension

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val applicationInitHook = activityOnCreateExtensionHook(
    "/SplashScreenActivity;"
)
