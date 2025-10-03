package app.revanced.patches.nunl.ads

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val mainActivityOnCreateHook = activityOnCreateExtensionHook(
    "/NUApplication;"
)
