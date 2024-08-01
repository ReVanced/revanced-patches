package app.revanced.patches.youtube.misc.extensions.hooks

import app.revanced.patches.shared.misc.extensions.extensionsHook

/**
 * Hooks the context when the app is launched as a regular application (and is not an embedded video playback).
 */
// Extension context is the Activity itself.
internal val applicationInitHook = extensionsHook {
    strings("Application creation", "Application.onCreate")
}
