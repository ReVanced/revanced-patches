package app.revanced.patches.twitch.misc.extension

import app.revanced.patches.shared.misc.extension.activityOnCreateExtensionHook

internal val initHook = activityOnCreateExtensionHook("/TwitchApplication;")
