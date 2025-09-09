package app.revanced.patches.reddit.customclients.baconreader.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val initHook = extensionHook {
    custom { method, _ ->
        method.definingClass == "Lcom/onelouder/baconreader/BaconReader;" && method.name == "onCreate"
    }
}
