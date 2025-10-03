package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patcher.string
import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook
import app.revanced.patches.shared.misc.extension.extensionHook
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE

/**
 * Hooks the context when the app is launched as a regular application (and is not an embedded video playback).
 */
// Extension context is the Activity itself.
internal val applicationInitHook = extensionHook {
    // Does _not_ resolve to the YouTube main activity.
    // Required as some hooked code runs before the main activity is launched.
    instructions(
        string("Application.onCreate"),
        string("Application creation")
    )
}

internal val applicationInitOnCrateHook = activityOnCreateExtensionHook(
    YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
)
