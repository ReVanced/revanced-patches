package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MenuGroupsUpdatedFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/SettingsMenuPresenter\$Event\$MenuGroupsUpdated;")
                && methodDef.name == "<init>"
    }
)
