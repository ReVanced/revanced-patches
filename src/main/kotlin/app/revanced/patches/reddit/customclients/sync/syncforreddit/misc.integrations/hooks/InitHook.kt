package app.revanced.patches.reddit.customclients.sync.syncforreddit.misc.integrations.hooks

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val initHook = integrationsHook(
    insertIndexResolver = { 1 }, // Insert after call to super class.
) {
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.type == "Lcom/laurencedawson/reddit_sync/RedditApplication;"
    }
}
