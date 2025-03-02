package app.revanced.patches.twitch.misc.extension

import app.revanced.patches.shared.misc.extension.extensionHook

internal val initHook = extensionHook {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/TwitchApplication;")
    }
}
