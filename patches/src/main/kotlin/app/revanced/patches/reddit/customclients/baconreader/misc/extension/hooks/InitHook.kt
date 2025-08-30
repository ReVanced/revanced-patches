package app.revanced.patches.reddit.customclients.baconreader.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val initHook = extensionHook(
    insertIndexResolver = { 1 },
) {
    custom { method, _ ->
        method.definingClass == "Lcom/onelouder/baconreader/BaconReader;" && method.name == "onCreate"
    }
}
