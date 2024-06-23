package app.revanced.patches.music.misc.integrations.hooks

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val applicationInitHook = integrationsHook {
    returns("V")
    parameters()
    strings("activity")
    custom { method, _ -> method.name == "onCreate" }
}
