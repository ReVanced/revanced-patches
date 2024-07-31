package app.revanced.patches.twitch.misc.extensions

import app.revanced.patches.shared.misc.extensions.extensionsHook

internal val initHook = extensionsHook {
    custom { method, classDef ->
        method.name == "onCreate" && classDef.endsWith("/TwitchApplication;")
    }
}
