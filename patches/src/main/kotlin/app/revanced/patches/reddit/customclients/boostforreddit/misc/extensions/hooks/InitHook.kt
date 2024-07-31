package app.revanced.patches.reddit.customclients.boostforreddit.misc.extensions.hooks

import app.revanced.patches.shared.misc.extensions.extensionsHook

internal val initHook = extensionsHook(
    insertIndexResolver = { 1 },
) {
    custom { method, _ ->
        method.definingClass == "Lcom/rubenmayayo/reddit/MyApplication;" && method.name == "onCreate"
    }
}
