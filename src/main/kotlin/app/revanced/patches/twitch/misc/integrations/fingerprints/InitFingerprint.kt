package app.revanced.patches.twitch.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint

internal object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/TwitchApplication;") &&
                methodDef.name == "onCreate"
    }
)