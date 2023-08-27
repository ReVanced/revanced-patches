package app.revanced.patches.twitch.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patches.twitch.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch.IntegrationsFingerprint

@Name("Init fingerprint")
@IntegrationsCompatibility
object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/TwitchApplication;") &&
                methodDef.name == "onCreate"
    }
)