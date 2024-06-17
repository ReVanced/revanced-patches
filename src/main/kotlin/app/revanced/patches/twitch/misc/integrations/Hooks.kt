package app.revanced.patches.twitch.misc.integrations

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val initHook = integrationsHook {
    custom { method, classDef ->
        classDef.endsWith("/TwitchApplication;") &&
            method.name == "onCreate"
    }
}
