package app.revanced.patches.reddit.customclients.boostforreddit.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val initHook = extensionHook(
    insertIndexResolver = { 1 },
) {
    custom { method, _ ->
        method.definingClass == "Lcom/rubenmayayo/reddit/MyApplication;" && method.name == "onCreate"
    }
}
