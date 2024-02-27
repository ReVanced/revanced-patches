package app.revanced.patches.reddit.utils.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object SettingsStatusLoadFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("Lapp/revanced/integrations/reddit/settingsmenu/SettingsStatus;") &&
                methodDef.name == "load"
    }
)