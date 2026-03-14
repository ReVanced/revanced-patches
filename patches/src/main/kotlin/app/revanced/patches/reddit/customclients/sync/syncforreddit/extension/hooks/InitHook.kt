package app.revanced.patches.reddit.customclients.sync.syncforreddit.extension.hooks

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val initHook = activityOnCreateExtensionHook(
    "Lcom/laurencedawson/reddit_sync/RedditApplication;"
)
