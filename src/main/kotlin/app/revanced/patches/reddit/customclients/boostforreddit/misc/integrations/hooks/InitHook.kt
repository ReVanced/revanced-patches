package app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.hooks

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val initHook = integrationsHook(
    insertIndexResolver = { 1 },
) {
    custom { method, _ ->
        method.definingClass == "Lcom/rubenmayayo/reddit/MyApplication;" && method.name == "onCreate"
    }
}
