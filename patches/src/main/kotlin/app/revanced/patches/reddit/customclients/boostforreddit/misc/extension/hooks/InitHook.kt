package app.revanced.patches.reddit.customclients.boostforreddit.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val initHook = activityOnCreateExtensionHook(
    "Lcom/rubenmayayo/reddit/MyApplication;"
)
