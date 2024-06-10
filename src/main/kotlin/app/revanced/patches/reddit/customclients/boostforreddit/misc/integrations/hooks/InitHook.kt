package app.revanced.patches.reddit.customclients.boostforreddit.misc.integrations.hooks

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val initHook = integrationsHook(
    insertIndexResolver = { 1 },
) {
    custom { methodDef, _ ->
        methodDef.definingClass == "Lcom/rubenmayayo/reddit/MyApplication;" && methodDef.name == "onCreate"
    }
}
