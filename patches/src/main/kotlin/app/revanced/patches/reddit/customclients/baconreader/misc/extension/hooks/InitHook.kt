package app.revanced.patches.reddit.customclients.baconreader.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val initHook = activityOnCreateExtensionHook(
    "Lcom/onelouder/baconreader/BaconReader;"
)
