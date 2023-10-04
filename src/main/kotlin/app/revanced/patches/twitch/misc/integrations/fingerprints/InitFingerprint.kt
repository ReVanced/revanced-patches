package app.revanced.patches.twitch.misc.integrations.fingerprints

import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch.IntegrationsFingerprint

object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/TwitchApplication;") &&
                methodDef.name == "onCreate"
    }
)