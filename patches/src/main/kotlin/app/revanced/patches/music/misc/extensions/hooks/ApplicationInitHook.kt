package app.revanced.patches.music.misc.extensions.hooks

import app.revanced.patches.shared.misc.extensions.extensionsHook

internal val applicationInitHook = extensionsHook {
    returns("V")
    parameters()
    strings("activity")
    custom { method, _ -> method.name == "onCreate" }
}
