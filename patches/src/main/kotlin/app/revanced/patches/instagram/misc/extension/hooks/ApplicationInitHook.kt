package app.revanced.patches.instagram.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val applicationInitHook = activityOnCreateExtensionHook(
    "/InstagramAppShell;"
)
