package app.revanced.patches.messenger.misc.extension

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val messengerApplicationOnCreateHook = activityOnCreateExtensionHook(
    "/MessengerApplication;"
)
