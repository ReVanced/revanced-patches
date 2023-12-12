package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MenuGroupsUpdatedFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsMenuPresenter\$Event\$MenuGroupsUpdated;")
                && methodDef.name == "<init>"
    }
)
