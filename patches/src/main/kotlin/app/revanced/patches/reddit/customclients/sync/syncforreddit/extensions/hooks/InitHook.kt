package app.revanced.patches.reddit.customclients.sync.syncforreddit.extensions.hooks

import app.revanced.patches.shared.misc.extensions.extensionsHook

internal val initHook = extensionsHook(
    insertIndexResolver = { 1 }, // Insert after call to super class.
) {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == "Lcom/laurencedawson/reddit_sync/RedditApplication;"
    }
}
