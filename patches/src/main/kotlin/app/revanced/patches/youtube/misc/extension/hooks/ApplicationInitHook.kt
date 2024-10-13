package app.revanced.patches.youtube.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

/**
 * Hooks the context when the app is launched as a regular application (and is not an embedded video playback).
 */
// Extension context is the Activity itself.
internal val applicationInitHook = extensionHook {
    strings("Application creation", "Application.onCreate")
}
