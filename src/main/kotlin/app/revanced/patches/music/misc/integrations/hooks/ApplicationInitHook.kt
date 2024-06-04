package app.revanced.patches.music.misc.integrations.hooks

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val applicationInitHook = integrationsHook {
    returns("V")
    parameters()
    strings("activity")
    custom { methodDef, _ -> methodDef.name == "onCreate" }
}
