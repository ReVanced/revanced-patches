package app.revanced.patches.twitch.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.integrationsHook

internal val initFingerprint = integrationsHook {
    custom { methodDef, classDef ->
        classDef.endsWith("/TwitchApplication;") &&
                methodDef.name == "onCreate"
    }
}