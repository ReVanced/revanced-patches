package app.revanced.patches.trakt.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RealmUserSettingsFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("RealmUserSettings;")
    }
)