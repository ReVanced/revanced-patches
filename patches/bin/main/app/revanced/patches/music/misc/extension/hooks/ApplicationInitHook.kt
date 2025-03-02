package app.revanced.patches.music.misc.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    returns("V")
    parameters()
    strings("activity")
    custom { method, _ -> method.name == "onCreate" }
}
