package app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val initHook = extensionHook(
    insertIndexResolver = { 1 }, // Insert after call to super class.
) {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == "Lcom/laurencedawson/reddit_sync/RedditApplication;"
    }
}
